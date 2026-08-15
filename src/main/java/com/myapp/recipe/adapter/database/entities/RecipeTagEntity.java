package com.myapp.recipe.adapter.database.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecipeTagEntity {

    Long id;
    Long recipeId;
    Long tagId;
    String tagName;
}
