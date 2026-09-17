package com.myapp.recipe.application;

import lombok.Getter;

@Getter
public class RecipeAlreadyExistsException extends RuntimeException {
    private final String recipeName;

    public RecipeAlreadyExistsException(String name) {
        super("Recipe with name '" + name + "' already exists");
        this.recipeName = name;
    }
}
