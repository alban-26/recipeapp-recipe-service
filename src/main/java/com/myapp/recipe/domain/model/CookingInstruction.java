package com.myapp.recipe.domain.model;

import java.util.Collection;

public record CookingInstruction(String instruction, Collection<RecipeIngredient> recipeIngredients) {

}
