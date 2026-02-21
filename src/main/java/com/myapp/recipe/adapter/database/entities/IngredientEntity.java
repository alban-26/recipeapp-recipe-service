package com.myapp.recipe.adapter.database.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngredientEntity {
    private Long id;
    private String name;
    private String category;
}
