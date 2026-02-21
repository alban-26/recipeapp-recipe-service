package com.myapp.recipe.adapter.messaging;



import com.myapp.recipe.domain.model.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecipeIngredientInfo {

    private String name;
    private ProductCategory category;
    private Double quantity;
    private String unit;

}

