package com.myapp.recipe.domain.model;

import com.my.common.api.UserId;
import lombok.With;

import java.time.Duration;
import java.util.Collection;

@With
public record Recipe(RecipeId id, String name, Collection<RecipeIngredient> recipeIngredients,
                     Collection<CookingInstruction> cookingInstructions,
                     int portions, Duration duration, UserId userId) {
}
