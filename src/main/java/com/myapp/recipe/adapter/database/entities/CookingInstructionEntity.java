package com.myapp.recipe.adapter.database.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CookingInstructionEntity {

    private Long id;
    private Long recipeId;
    private int stepNumber;
    private String instruction;

    private List<InstructionIngredientEntity> instructionIngredients;
}
