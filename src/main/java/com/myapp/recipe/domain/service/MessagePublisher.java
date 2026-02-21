package com.myapp.recipe.domain.service;

import com.myapp.recipe.domain.model.RecipeInfo;

public interface MessagePublisher {
    void publishRecipeInfo(RecipeInfo recipeInfo);
}

