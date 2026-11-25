package com.myapp.recipe.adapter.restapi;


import com.my.common.api.UserId;
import com.my.common.api.pagination.PageResult;
import com.myapp.recipe.adapter.ErrorResponse;
import com.myapp.recipe.adapter.RecipeConverter;
import com.myapp.recipe.adapter.database.RecipePageRequestImpl;
import com.myapp.recipe.application.RecipeService;
import com.myapp.recipe.domain.model.*;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.openapitools.api.RecipesApi;
import org.openapitools.model.IngredientDto;
import org.openapitools.model.RecipeDto;
import org.openapitools.model.RecipePageDto;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Path("/recipes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class RecipeResource implements RecipesApi  {

    private final RecipeService recipeService;
    private final RecipeConverter recipeConverter;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;



    @Inject
    public RecipeResource(RecipeService recipeService, RecipeConverter recipeConverter) {
        this.recipeService = recipeService;
        this.recipeConverter = recipeConverter;
    }

    @Override
    public Response createRecipe(RecipeDto recipeDto) {

        User currentUser = User.fromToken(jwt);
        log.info("User {} is creating recipe {}", currentUser.id(), recipeDto.getName());
        // Anlegen heisst immer "neu": eine mitgeschickte ID wird ignoriert, sonst
        // wuerde hier ein bestehendes (auch fremdes) Rezept ueberschrieben.
        Recipe recipe = recipeConverter.dtoToDomain(recipeDto)
                .withId(new RecipeId(0))
                .withUserId(currentUser.id());
        Recipe createdRecipe = recipeService.createRecipe(recipe);


        return Response.created(URI.create("/recipes/" + createdRecipe.id().id())).entity(createdRecipe.id().id()).build();
    }


    @Override
    public Response deleteRecipe(Integer id) {
        User currentUser = User.fromToken(jwt);
        log.info("User {} is deleting recipe {}", currentUser.id(), id);

        RecipeId recipeId = new RecipeId(id);
        if (findOwnRecipe(recipeId, currentUser).isEmpty()) {
            return recipeNotFound();
        }

        recipeService.deleteById(recipeId);
        return Response.ok().build();
    }


    @Override
    public Response getRecipeById(Integer id) {
        User currentUser = User.fromToken(jwt);
        Optional<Recipe> recipeOpt = findOwnRecipe(new RecipeId(id), currentUser);

        if (recipeOpt.isPresent()) {
            return Response.ok(recipeOpt.get()).build();
        } else {
            return recipeNotFound();
        }
    }

    /**
     * Liefert das Rezept nur, wenn es dem angemeldeten Nutzer gehoert. Ein
     * fremdes Rezept wird genauso behandelt wie ein nicht vorhandenes, damit
     * sich ueber die Antwort nicht herausfinden laesst, welche IDs es gibt.
     */
    private Optional<Recipe> findOwnRecipe(RecipeId recipeId, User currentUser) {
        return recipeService.getById(recipeId)
                .filter(recipe -> isOwner(recipe, currentUser));
    }

    private static boolean isOwner(Recipe recipe, User currentUser) {
        String owner = recipe.userId() != null ? recipe.userId().value() : null;
        String current = currentUser.id() != null ? currentUser.id().value() : null;
        return owner != null && owner.equals(current);
    }

    private static Response recipeNotFound() {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(ErrorResponse.of("RECIPE_NOT_FOUND", "Recipe not found")).build();
    }

    @Override
    public Response getRecipes(Integer page, Integer size, String sort, String searchQuery, List<String> tags) {
        User currentUser = User.fromToken(jwt);

        PageResult<Recipe> resultPage = recipeService.getAllByUser(
                new UserId(currentUser.id().value()),
                new RecipePageRequestImpl(page, size, searchQuery, new HashSet<>(tags))
        );

        RecipePageDto response = new RecipePageDto();

        response.setContent(
                resultPage.content()
                        .stream()
                        .map(recipeConverter::domainToDto)
                        .toList()
        );

        response.setPage(resultPage.page());
        response.setSize(resultPage.size());
        response.setTotalElements(resultPage.totalElements());
        response.setLast(resultPage.last());
        response.setTotalPages(resultPage.totalPages());

        return Response.ok(response).build();
    }

    @Override
    public Response getUsedTags() {
        User currentUser = User.fromToken(jwt);
        return Response.ok(recipeService.getTags(currentUser.id()).stream().map(Tag::value).toList()).build();
    }


    @Override
    public Response updateRecipe(RecipeDto recipeDto) {
        User currentUser = User.fromToken(jwt);
        log.info("User {} updating recipe {}", currentUser.id(), recipeDto.getId());

        Recipe updatedRecipe = recipeConverter.dtoToDomain(recipeDto).withUserId(currentUser.id());
        // updateRecipe prueft, dass das Rezept existiert und dem Nutzer gehoert,
        // und meldet einen Namenskonflikt nur, wenn der Name wirklich geaendert
        // wurde. Vorher lief das ueber createRecipe - jedes Speichern mit
        // unveraendertem Namen endete deshalb mit 409.
        Recipe savedRecipe = recipeService.updateRecipe(updatedRecipe);
        RecipeDto recipeDto1 = recipeConverter.domainToDto(savedRecipe);
        return Response.ok(recipeDto1).build();
    }

    @POST
    @Consumes({"multipart/form-data"})
    @Produces({"application/json"})
    @Path("/{id}/image")
    @Blocking
    public Response uploadRecipeImage(@PathParam("id") Long id, @FormParam("image") InputStream imageInputStream) {
        User currentUser = User.fromToken(jwt);
        log.info("User {} is saving recipe image with ID {}", currentUser.id(), id);
        if (id == null || findOwnRecipe(new RecipeId(id), currentUser).isEmpty()) {
            return recipeNotFound();
        }
        recipeService.saveRecipeImage(id, imageInputStream);

        return Response.ok().entity("Image uploaded successfully").build();
    }

    @GET
    @Produces({"image/jpeg", "image/png"})
    @Path("/{id}/image")
    @Blocking
    public Response downloadRecipeImage(@PathParam("id") Long id) {
        User currentUser = User.fromToken(jwt);
        // Fremdes Rezept: dieselbe Antwort wie "kein Bild vorhanden".
        boolean own = id != null && findOwnRecipe(new RecipeId(id), currentUser).isPresent();
        File imageFile = own ? recipeService.loadRecipeImage(id) : null;
        if (imageFile == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.of("RECIPE_IMAGE_NOT_FOUND", "Recipe image not found")).build();
        }

        return Response.ok(imageFile).header("Content-Disposition", "attachment; filename=\"" + imageFile.getName() + "\"").build();
    }

    @Override
    public Response getIngredients() {
        User currentUser = User.fromToken(jwt);
        List<Ingredient> ingredients = recipeService.getIngredients(new UserId(currentUser.id().value()));
        if (ingredients.isEmpty()) {
            return Response.noContent().build();
        }
        List<IngredientDto> ingredientDtos = ingredients.stream().map(ingredient -> IngredientDto.builder().name(ingredient.name()).category(IngredientDto.CategoryEnum.valueOf(ingredient.productCategory().name())).build()).toList();
        return Response.ok(ingredientDtos).build();

    }
}
