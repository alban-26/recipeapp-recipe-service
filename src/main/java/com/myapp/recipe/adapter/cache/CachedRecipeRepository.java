package com.myapp.recipe.adapter.cache;


import com.my.common.api.UserId;
import com.my.common.api.pagination.PageRequest;
import com.my.common.api.pagination.PageResult;
import com.myapp.recipe.domain.model.Ingredient;
import com.myapp.recipe.domain.model.Recipe;
import com.myapp.recipe.domain.model.RecipeId;
import com.myapp.recipe.domain.service.RecipeRepository;

import java.util.List;
import java.util.Optional;

public class CachedRecipeRepository implements RecipeRepository {
    @Override
    public List<Ingredient> fetchIngredients(UserId userId) {
        return List.of();
    }

    @Override
    public Optional<Recipe> findById(RecipeId recipeId) {
        return Optional.empty();
    }

    @Override
    public List<Recipe> findAll() {
        return List.of();
    }

    @Override
    public List<Recipe> findAllByUser(UserId userId) {
        return List.of();
    }

    @Override
    public PageResult<Recipe> findAllByUser(UserId userId, PageRequest pageRequest) {
        return null;
    }

    @Override
    public Recipe save(Recipe recipe) {
        return null;
    }

    @Override
    public Recipe update(Recipe recipe) {
        return null;
    }

    @Override
    public void deleteById(RecipeId recipeId) {

    }
}
