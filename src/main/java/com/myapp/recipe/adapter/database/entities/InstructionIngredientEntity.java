package com.myapp.recipe.adapter.database.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstructionIngredientEntity {
    private Long id;
    private Long cookingInstructionId;
    private Long recipeIngredientId;

    private String ingredientName;      // From join
    private String ingredientCategory;  // From join
    private String unit;                // From recipe_ingredient

    private double quantity;
    private double totalQuantity;       // From recipe_ingredient (optional, for validation)
}
