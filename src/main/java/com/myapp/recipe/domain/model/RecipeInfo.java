package com.myapp.recipe.domain.model;

import com.my.common.api.UserId;

import java.util.Collection;

public record RecipeInfo(RecipeId id, String name, Collection<RecipeIngredient> recipeIngredients, int portions, UserId userId) {
}
