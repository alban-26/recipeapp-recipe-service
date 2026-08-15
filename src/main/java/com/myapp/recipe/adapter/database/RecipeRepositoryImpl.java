package com.myapp.recipe.adapter.database;



import com.my.common.api.UserId;
import com.my.common.api.pagination.PageRequest;
import com.my.common.api.pagination.PageResult;
import com.myapp.recipe.adapter.RecipeConverter;
import com.myapp.recipe.adapter.database.entities.*;
import com.myapp.recipe.domain.model.*;
import com.myapp.recipe.domain.service.RecipeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;
import java.util.stream.Collectors;

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
    public List<Ingredient> fetchIngredients(UserId userId) {
        List<IngredientEntity> ingredientEntities = recipeMapper.selectIngredientsByUserId(userId.value());
        return ingredientEntities.stream().map(ingredientEntity -> new Ingredient(ingredientEntity.getName(), ProductCategory.valueOf(ingredientEntity.getCategory()))).toList();
    }

    @Override
    public Set<Tag> fetchTags(UserId userId) {
        List<String> tags = recipeMapper.selectTagsByUser(userId.value());
        return tags.stream().map(Tag::new).collect(Collectors.toSet());
    }


    @Override
    public Recipe save(Recipe recipe) {
        RecipeEntity entity = recipeConverter.domainToEntity(recipe);
        recipeMapper.insertRecipe(entity);

        saveTags(entity);
        saveIngredients(entity);
        saveCookingInstructions(entity);

        return recipeConverter.entityToDomain(entity);
    }

    @Override
    public Recipe update(Recipe recipe) {
        RecipeEntity entity = recipeConverter.domainToEntity(recipe);
        recipeMapper.updateRecipe(entity);

        deleteRelatedData(entity.getId());

        saveTags(entity);
        saveIngredients(entity);
        saveCookingInstructions(entity);

        return recipeConverter.entityToDomain(entity);
    }

    private void deleteRelatedData(Long recipeId) {
        recipeMapper.deleteInstructionIngredientsByRecipeId(recipeId);
        recipeMapper.deleteCookingInstructionsByRecipeId(recipeId);
        recipeMapper.deleteRecipeIngredientsByRecipeId(recipeId);
        recipeMapper.deleteRecipeTagsByRecipeId(recipeId);
    }


    private void saveTags(RecipeEntity entity) {
        for (RecipeTagEntity recipeTag : entity.getTags()) {
            recipeTag.setRecipeId(entity.getId());
            recipeTag.setTagId(resolveTagId(recipeTag.getTagName()));
            recipeMapper.insertRecipeTag(recipeTag);
        }
    }

    private Long resolveTagId(String tagName) {
        TagEntity tag = new TagEntity(0L, tagName);
        recipeMapper.insertTag(tag);
        return tag.getId() != 0
                ? tag.getId()
                : recipeMapper.findTagIdByName(tagName);
    }


    private void saveIngredients(RecipeEntity entity) {
        for (RecipeIngredientEntity ingredient : entity.getRecipeIngredients()) {
            ingredient.setRecipeId(entity.getId());
            ingredient.setIngredientId(resolveIngredientId(ingredient));
            recipeMapper.insertRecipeIngredient(ingredient);
        }
    }

    private Long resolveIngredientId(RecipeIngredientEntity ingredient) {
        IngredientEntity ingredientEntity = new IngredientEntity(
                0L, ingredient.getIngredientName(), ingredient.getIngredientCategory());
        recipeMapper.insertIngredient(ingredientEntity);
        return ingredientEntity.getId() != 0
                ? ingredientEntity.getId()
                : recipeMapper.findByName(ingredient.getIngredientName());
    }


    private void saveCookingInstructions(RecipeEntity entity) {
        for (CookingInstructionEntity instruction : entity.getCookingInstructions()) {
            instruction.setRecipeId(entity.getId());
            recipeMapper.insertCookingInstruction(instruction);
            saveInstructionIngredients(instruction, entity.getRecipeIngredients());
        }
    }

    private void saveInstructionIngredients(CookingInstructionEntity instruction,
                                            List<RecipeIngredientEntity> recipeIngredients) {
        for (InstructionIngredientEntity instructionIngredient : instruction.getInstructionIngredients()) {
            instructionIngredient.setCookingInstructionId(instruction.getId());
            instructionIngredient.setRecipeIngredientId(
                    findRecipeIngredientId(instructionIngredient, recipeIngredients));
            recipeMapper.insertInstructionIngredient(instructionIngredient);
        }
    }

    private Long findRecipeIngredientId(InstructionIngredientEntity instructionIngredient,
                                        List<RecipeIngredientEntity> recipeIngredients) {
        return recipeIngredients.stream()
                .filter(ri -> ri.getIngredientName().equals(instructionIngredient.getIngredientName())
                        && ri.getUnit().equals(instructionIngredient.getUnit()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot find recipeIngredientId for " + instructionIngredient.getIngredientName()))
                .getId();
    }


    @Override
    public void deleteById(RecipeId id) {
        recipeMapper.deleteRecipe(id.id());
    }


/*
    private void saveTags(Long recipeId, Set<String> tags) {
        recipeMapper.deleteRecipeTagsByRecipeId(recipeId);   // für Update: alte Links weg
        if (tags == null) return;

        for (String name : tags) {
            recipeMapper.insertTag(name);                    // anlegen, falls neu
            Long tagId = recipeMapper.findTagIdByName(name); // id holen (auch wenn schon vorhanden)
            recipeMapper.insertRecipeTag(recipeId, tagId);
        }
    }
*/


    @Override
    public PageResult<Recipe> findAllByUser(UserId userId, RecipePageRequest recipePageRequest) {
        List<RecipeEntity> entities =
                recipeMapper.selectRecipesByUserPaged(
                        userId.value(),
                        recipePageRequest.size(),
                        recipePageRequest.offset(), recipePageRequest.searchQuery(), recipePageRequest.getTags().stream().toList(), recipePageRequest.getTags().size());

        long total = recipeMapper.countRecipesByUser(userId.value(), recipePageRequest.searchQuery(), recipePageRequest.getTags().stream().toList(), recipePageRequest.getTags().size());

        int totalPages = (int) Math.ceil((double) total / recipePageRequest.size());
        boolean last = recipePageRequest.page() >= totalPages - 1;

        return new PageResult<>(
                entities.stream()
                        .map(recipeConverter::entityToDomain)
                        .toList(),
                total,
                totalPages,
                recipePageRequest.page(),
                recipePageRequest.size(),
                last
        );
    }


}
