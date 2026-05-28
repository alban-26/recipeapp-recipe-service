package com.myapp.recipe.adapter.restapi;

import com.my.common.api.UserId;
import com.myapp.recipe.adapter.ErrorResponse;
import com.myapp.recipe.adapter.RecipeConverter;
import com.myapp.recipe.application.RecipeService;
import com.myapp.recipe.domain.model.Ingredient;
import com.myapp.recipe.domain.model.Recipe;
import com.myapp.recipe.domain.model.RecipeId;
import com.myapp.recipe.domain.model.User;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.common.annotation.NonBlocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.openapitools.api.RecipesApi;
import org.openapitools.model.IngredientDto;
import org.openapitools.model.RecipeDto;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@Path("/recipes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@NonBlocking
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
        log.info("User {} is creating recipe {}", currentUser.username(), recipeDto.getName());
        Recipe recipe = recipeConverter.dtoToDomain(recipeDto).withUserId(currentUser.id());
        Recipe createdRecipe = recipeService.createRecipe(recipe);


        return Response.created(URI.create("/recipes/" + createdRecipe.id().id())).entity(createdRecipe.id().id()).build();
    }


    @Override
    public Response deleteRecipe(Integer id) {
        User currentUser = User.fromToken(jwt);
        log.info("User {} is deleting recipe {}", currentUser.username(), id);

        recipeService.deleteById(new RecipeId(id));
        return Response.ok().build();
    }


    @Override
    public Response getRecipeById(Integer id) {
        RecipeId recipeId = new RecipeId(id);
        Optional<Recipe> recipeOpt = recipeService.getById(recipeId);

        if (recipeOpt.isPresent()) {
            return Response.ok(recipeOpt.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse("Recipe not found")).build();
        }
    }

    @Override
    public Response getRecipes() {
        User currentUser = User.fromToken(jwt);
        List<Recipe> recipes = recipeService.getAllByUser(new UserId(currentUser.id().value()));
        if (recipes.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(recipes.stream().map(recipeConverter::domainToDto)).build();
    }

    @Override
    public Response updateRecipe(RecipeDto recipeDto) {
        User currentUser = User.fromToken(jwt);
        log.info("User {} updating recipe {}", currentUser.username(), recipeDto.getId());

        Recipe updatedRecipe = recipeConverter.dtoToDomain(recipeDto).withUserId(currentUser.id());
        Recipe savedRecipe = recipeService.createRecipe(updatedRecipe);
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
        recipeService.saveRecipeImage(id, imageInputStream);

        return Response.ok().entity("Image uploaded successfully").build();
    }

    @GET
    @Produces({"image/jpeg", "image/png"})
    @Path("/{id}/image")
    @Blocking
    public Response downloadRecipeImage(@PathParam("id") Long id) {
        File imageFile = recipeService.loadRecipeImage(id);
        if (imageFile == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse("Recipe image not found")).build();
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
        return Response.ok(ingredients.stream().map(ingredient -> IngredientDto.builder().name(ingredient.name()).category(IngredientDto.CategoryEnum.valueOf(ingredient.productCategory().name())))).build();

    }
}
