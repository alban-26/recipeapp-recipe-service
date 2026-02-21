package com.myapp.recipe.adapter.database;

import com.my.common.api.UserId;
import com.myapp.recipe.adapter.RecipeConverter;
import com.myapp.recipe.adapter.database.entities.*;
import com.myapp.recipe.domain.model.Recipe;
import com.myapp.recipe.domain.model.RecipeId;
import com.myapp.recipe.domain.service.RecipeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RecipeRepositoryImpl implements RecipeRepository {

    private final RecipeMapper recipeMapper;
    private final RecipeConverter recipeConverter;

    @Inject
    public RecipeRepositoryImpl(RecipeMapper recipeMapper, RecipeConverter recipeConverter) {
        this.recipeMapper = recipeMapper;
        this.recipeConverter = recipeConverter;
    }

    @Override
    public Optional<Recipe> findById(RecipeId id) {
        return Optional.ofNullable(recipeMapper.selectRecipe(id.id()))
                .map(recipeConverter::entityToDomain);
    }


    @Override
    public List<Recipe> findAll() {
        List<RecipeEntity> recipeEntities = recipeMapper.selectRecipes();
        return recipeEntities.stream().map(recipeConverter::entityToDomain).toList();
    }

    @Override
    public List<Recipe> findAllByUser(UserId userId) {
        List<RecipeEntity> recipeEntities = recipeMapper.selectRecipesByUser(userId.value());
        return recipeEntities.stream().map(recipeConverter::entityToDomain).toList();
    }


    @Override
    public Recipe save(Recipe recipe) {

        RecipeEntity entity = recipeConverter.domainToEntity(recipe);

        recipeMapper.insertRecipe(entity);

        for (RecipeIngredientEntity ingredient : entity.getRecipeIngredients()) {
            ingredient.setRecipeId(entity.getId());

            IngredientEntity ingredientEntity = new IngredientEntity(0L, ingredient.getIngredientName(), ingredient.getIngredientCategory());
            recipeMapper.insertIngredient(
                    ingredientEntity);


            if (ingredientEntity.getId() != 0) {
                ingredient.setIngredientId(ingredientEntity.getId());
            } else {
                ingredient.setIngredientId(recipeMapper.findByName(ingredient.getIngredientName()));
            }

            recipeMapper.insertRecipeIngredient(ingredient);
        }

        for (CookingInstructionEntity instructionEntity : entity.getCookingInstructions()) {
            instructionEntity.setRecipeId(entity.getId());
            recipeMapper.insertCookingInstruction(instructionEntity);

            for (InstructionIngredientEntity instructionIngredient : instructionEntity.getInstructionIngredients()) {
                instructionIngredient.setCookingInstructionId(instructionEntity.getId());

                Long recipeIngredientId = entity.getRecipeIngredients().stream()
                        .filter(ri -> ri.getIngredientName().equals(instructionIngredient.getIngredientName())
                                && ri.getUnit().equals(instructionIngredient.getUnit()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(
                                "Cannot find recipeIngredientId for " + instructionIngredient.getIngredientName()))
                        .getId();

                instructionIngredient.setRecipeIngredientId(recipeIngredientId);

                recipeMapper.insertInstructionIngredient(instructionIngredient);
            }
        }

        return recipeConverter.entityToDomain(entity);
    }


    @Override
    public Recipe update(Recipe recipe) {
        RecipeEntity entity = recipeConverter.domainToEntity(recipe);

        recipeMapper.updateRecipe(entity);

        recipeMapper.deleteInstructionIngredientsByRecipeId(entity.getId());
        recipeMapper.deleteCookingInstructionsByRecipeId(entity.getId());
        recipeMapper.deleteRecipeIngredientsByRecipeId(entity.getId());


        for (RecipeIngredientEntity ingredient : entity.getRecipeIngredients()) {
            ingredient.setRecipeId(entity.getId());
            IngredientEntity ingredientEntity = new IngredientEntity(0L, ingredient.getIngredientName(), ingredient.getIngredientCategory());
            recipeMapper.insertIngredient(
                    ingredientEntity);

            if (ingredientEntity.getId() != 0) {
                ingredient.setIngredientId(ingredientEntity.getId());
            } else {
                ingredient.setIngredientId(recipeMapper.findByName(ingredient.getIngredientName()));
            }
            recipeMapper.insertRecipeIngredient(ingredient);
        }

        for (CookingInstructionEntity instructionEntity : entity.getCookingInstructions()) {
            instructionEntity.setRecipeId(entity.getId());
            recipeMapper.insertCookingInstruction(instructionEntity);

            for (InstructionIngredientEntity instructionIngredient : instructionEntity.getInstructionIngredients()) {
                instructionIngredient.setCookingInstructionId(instructionEntity.getId());
                Long recipeIngredientId = entity.getRecipeIngredients().stream()
                        .filter(ri -> ri.getIngredientName().equals(instructionIngredient.getIngredientName())
                                && ri.getUnit().equals(instructionIngredient.getUnit()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(
                                "Cannot find recipeIngredientId for " + instructionIngredient.getIngredientName()))
                        .getId();

                instructionIngredient.setRecipeIngredientId(recipeIngredientId);
                recipeMapper.insertInstructionIngredient(instructionIngredient);
            }
        }

        return recipeConverter.entityToDomain(entity);
    }


    @Override
    public void deleteById(RecipeId id) {
        recipeMapper.deleteRecipe(id.id());
    }


}
