package com.myapp.recipe.application;


import com.my.common.api.AbstractAccess;
import com.my.common.api.UserId;
import com.myapp.recipe.adapter.restapi.FileService;
import com.myapp.recipe.domain.model.Ingredient;
import com.myapp.recipe.domain.model.Recipe;
import com.myapp.recipe.domain.model.RecipeId;
import com.myapp.recipe.domain.model.RecipeInfo;
import com.myapp.recipe.domain.service.MessagePublisher;
import com.myapp.recipe.domain.service.RecipeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PathParam;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Slf4j
public class RecipeService extends AbstractAccess<RecipeRepository, Recipe, RecipeId> {

    private final RecipeRepository recipeRepository;
    private final FileService fileService;
    private static final Path IMAGE_PATH = Paths.get("/images");
    private final MessagePublisher messagePublisher;


    protected RecipeService() {
        super(null);
        this.recipeRepository = null;
        this.fileService = null;
        this.messagePublisher = null;
    }

    @Inject
    public RecipeService(RecipeRepository recipeRepository, FileService fileService, MessagePublisher messagePublisher) {
        super(recipeRepository);
        this.recipeRepository = recipeRepository;
        this.fileService = fileService;
        this.messagePublisher = messagePublisher;
    }

    public Recipe createRecipe(Recipe recipe) {
        Recipe savedRecipe = super.save(recipe);
        log.debug("Recipe saved with ID: {}", savedRecipe.id());

        RecipeInfo info = new RecipeInfo(
                recipe.id(),
                savedRecipe.name(),
                savedRecipe.recipeIngredients(),
                savedRecipe.portions(),
                savedRecipe.userId()
        );

        try {
            messagePublisher.publishRecipeInfo(info);
            log.debug("Recipe with ID {} published", info.id());
        } catch (Exception e) {
            log.error("Failed to publish recipe with ID {}", info.id(), e);
        }

        return savedRecipe;
    }

    public List<Ingredient> getIngredients(UserId userId) {
        return recipeRepository.fetchIngredients(userId);
    }

    public void deleteById(RecipeId id) {
        Optional<Recipe> recipeOpt = this.getById(id);
        if (recipeOpt.isEmpty()) {
            throw new NotFoundException("Attempted to delete non-existent recipe with ID {}");
        }
        recipeRepository.deleteById(recipeOpt.get().id());
        Path imageDir = resolvePathByRecipe(recipeOpt.get().id().id());
        fileService.deleteImage(imageDir);
        log.info("Deleted recipe with ID {}", id);

    }

    public void saveRecipeImage(@PathParam("id") Long id, @FormParam("image") InputStream imageInputStream) {

        Path imagePath = resolvePathByRecipe(id);
        try {
            fileService.saveImage(imagePath, imageInputStream);
            log.info("Recipe image saved successfully for recipe id={} at path={}", id, imagePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File loadRecipeImage(Long id) {
        Path path = resolvePathByRecipe(id);
        return fileService.getImage(path);
    }

    private static Path resolvePathByRecipe(Long id) {
        return IMAGE_PATH.resolve("recipe_" + id + ".jpg");
    }

    @Override
    protected boolean isNew(Recipe recipe) {
        return recipe.id() == null || recipe.id().id() == 0;
    }

    @Override
    protected RecipeId getId(Recipe recipe) {
        return recipe.id();
    }

}
