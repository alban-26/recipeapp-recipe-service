package com.myapp.recipe.adapter;

import com.myapp.recipe.adapter.database.entities.RecipeEntity;
import com.myapp.recipe.domain.model.Recipe;
import org.openapitools.model.RecipeDto;


public interface RecipeConverter extends Converter<Recipe, RecipeDto, RecipeEntity> {


}
