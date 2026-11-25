package com.myapp.recipe.domain.service;

import com.myapp.recipe.domain.model.RecipeInfo;

public interface MessagePublisher {
    /** Neues Rezept. */
    void publishRecipeInfo(RecipeInfo recipeInfo);

    /**
     * Geaendertes Rezept.
     *
     * @param previousName Name vor der Aenderung (darf dem neuen Namen gleichen)
     */
    void publishRecipeUpdated(RecipeInfo recipeInfo, String previousName);

    /** Geloeschtes Rezept. */
    void publishRecipeDeleted(RecipeInfo recipeInfo);
}

