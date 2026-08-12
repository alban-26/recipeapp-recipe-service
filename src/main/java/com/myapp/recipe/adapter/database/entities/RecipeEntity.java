package com.myapp.recipe.adapter.database.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeEntity {

    private Long id;
    private String name;
    private int portions;
    private Duration duration;
    private List<RecipeIngredientEntity> recipeIngredients;
    private List<CookingInstructionEntity> cookingInstructions;
    private Set<String> tags;
    private String userId;

}
