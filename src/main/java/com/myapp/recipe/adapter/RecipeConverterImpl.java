package com.myapp.recipe.adapter;

import com.my.common.api.UserId;
import com.myapp.recipe.adapter.database.entities.*;
import com.myapp.recipe.domain.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import org.openapitools.model.CookingInstructionDto;
import org.openapitools.model.RecipeDto;
import org.openapitools.model.RecipeIngredientDto;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

@ApplicationScoped
public class RecipeConverterImpl implements RecipeConverter {

    @Override
    public Recipe dtoToDomain(RecipeDto recipeDto) {
        return new Recipe(
                recipeDto.getId() != null ? new RecipeId(recipeDto.getId()) : new RecipeId(0L),
                recipeDto.getName(),
                createRecipeIngredientsFromDtos(recipeDto.getRecipeIngredients()),
                createCookingInstructionsFromDtos(recipeDto.getCookingInstructions()),
                recipeDto.getPortions(),
                Duration.parse(recipeDto.getDuration()),
                null
        );
    }

    public Recipe dtoToDomain(RecipeDto recipeDto, UserId userId) {
        return new Recipe(
                new RecipeId(recipeDto.getId()),
                recipeDto.getName(),
                createRecipeIngredientsFromDtos(recipeDto.getRecipeIngredients()),
                createCookingInstructionsFromDtos(recipeDto.getCookingInstructions()),
                recipeDto.getPortions(),
                Duration.parse(recipeDto.getDuration()),
                userId
        );
    }

    private static Collection<RecipeIngredient> createRecipeIngredientsFromDtos(List<RecipeIngredientDto> dtos) {
        return dtos.stream()
                .map(dto -> new RecipeIngredient(
                        new Ingredient(dto.getName(), ProductCategory.valueOf(dto.getCategory().name())),
                        dto.getQuantity(),
                        Unit.fromAbbreviation(dto.getUnit())))
                .toList();
    }

    private static Collection<CookingInstruction> createCookingInstructionsFromDtos(List<CookingInstructionDto> dtos) {
        return dtos.stream()
                .map(dto -> new CookingInstruction(
                        dto.getInstruction(),
                        createRecipeIngredientsFromDtos(dto.getRecipeIngredients())))
                .toList();
    }

    @Override
    public Recipe entityToDomain(RecipeEntity recipeEntity) {
        return new Recipe(
                new RecipeId(recipeEntity.getId()),
                recipeEntity.getName(),
                createRecipeIngredientsFromEntities(recipeEntity.getRecipeIngredients()),
                createCookingInstructionsFromEntities(recipeEntity.getCookingInstructions()),
                recipeEntity.getPortions(),
                recipeEntity.getDuration(),
                new UserId(recipeEntity.getUserId())
        );
    }

    private static Collection<RecipeIngredient> createRecipeIngredientsFromEntities(List<RecipeIngredientEntity> entities) {
        return entities.stream()
                .map(e -> new RecipeIngredient(
                        new Ingredient(e.getIngredientName(), ProductCategory.valueOf(e.getIngredientCategory())),
                        e.getTotalQuantity(),
                        Unit.fromAbbreviation(e.getUnit())))
                .toList();
    }

    private static Collection<CookingInstruction> createCookingInstructionsFromEntities(List<CookingInstructionEntity> entities) {
        return entities.stream()
                .map(e -> new CookingInstruction(
                        e.getInstruction(),
                        createRecipeIngredientsFromInstructionEntities(e.getInstructionIngredients())))
                .toList();
    }

    private static Collection<RecipeIngredient> createRecipeIngredientsFromInstructionEntities(List<InstructionIngredientEntity> entities) {
        return entities.stream()
                .map(e -> new RecipeIngredient(
                        new Ingredient(e.getIngredientName(), ProductCategory.valueOf(e.getIngredientCategory())),
                        e.getQuantity(),
                        Unit.fromAbbreviation(e.getUnit())))
                .toList();
    }

    @Override
    public RecipeDto domainToDto(Recipe recipe) {
        return new RecipeDto(
                recipe.id().id(),
                recipe.name(),
                createRecipeIngredientDtos(recipe.recipeIngredients()),
                createCookingInstructionDtos(recipe.cookingInstructions()),
                recipe.portions(),
                recipe.duration().toString()
        );
    }

    private static List<RecipeIngredientDto> createRecipeIngredientDtos(Collection<RecipeIngredient> ingredients) {
        return ingredients.stream()
                .map(i -> new RecipeIngredientDto(
                        i.ingredient().name(),
                        RecipeIngredientDto.CategoryEnum.valueOf(i.ingredient().productCategory().name()),
                        i.quantity(),
                        i.unit().getAbbreviation()))
                .toList();
    }

    private static List<CookingInstructionDto> createCookingInstructionDtos(Collection<CookingInstruction> instructions) {
        return instructions.stream()
                .map(ci -> new CookingInstructionDto(
                        ci.instruction(),
                        createRecipeIngredientDtos(ci.recipeIngredients())))
                .toList();
    }

    @Override
    public RecipeEntity domainToEntity(Recipe recipe) {
        RecipeEntity entity = new RecipeEntity();
        entity.setId(recipe.id().id());
        entity.setName(recipe.name());
        entity.setPortions(recipe.portions());
        entity.setDuration(recipe.duration());

        entity.setRecipeIngredients(createRecipeIngredientEntities(recipe.recipeIngredients(), recipe.id()));
        entity.setCookingInstructions(createCookingInstructionEntities(recipe.cookingInstructions(), recipe.id()));
        entity.setUserId(recipe.userId().value());
        return entity;
    }

    private static List<RecipeIngredientEntity> createRecipeIngredientEntities(Collection<RecipeIngredient> ingredients, RecipeId recipeId) {
        return ingredients.stream().map(ri -> {
            RecipeIngredientEntity e = new RecipeIngredientEntity();
            e.setId(0L);
            e.setRecipeId(recipeId.id());

            e.setIngredientId(0L);

            e.setIngredientName(ri.ingredient().name());
            e.setIngredientCategory(ri.ingredient().productCategory().name());
            e.setTotalQuantity(ri.quantity());
            e.setUnit(ri.unit().name());
            return e;
        }).toList();
    }

    private static List<CookingInstructionEntity> createCookingInstructionEntities(Collection<CookingInstruction> instructions, RecipeId recipeId) {
        int[] stepCounter = {1};
        return instructions.stream().map(ci -> {
            CookingInstructionEntity e = new CookingInstructionEntity();
            e.setId(null);
            e.setRecipeId(recipeId.id());
            e.setStepNumber(stepCounter[0]++);
            e.setInstruction(ci.instruction());

            e.setInstructionIngredients(createInstructionIngredientEntities(ci.recipeIngredients()));
            return e;
        }).toList();
    }

    private static List<InstructionIngredientEntity> createInstructionIngredientEntities(Collection<RecipeIngredient> instructionIngredients) {
        return instructionIngredients.stream().map(ii -> {
            InstructionIngredientEntity e = new InstructionIngredientEntity();
            e.setId(0L);
            e.setCookingInstructionId(0L);
            e.setRecipeIngredientId(0L);

            e.setIngredientName(ii.ingredient().name());
            e.setIngredientCategory(ii.ingredient().productCategory().name());
            e.setUnit(ii.unit().name());
            e.setQuantity(ii.quantity());
            return e;
        }).toList();
    }
}
