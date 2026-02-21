package com.myapp.recipe.adapter.database.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeIngredientEntity {

    private Long id;
    private Long recipeId;
    private Long ingredientId;

    private String ingredientName;      // From joined ingredient
    private String ingredientCategory;  // From joined ingredient

    private double totalQuantity;
    private String unit;

}
