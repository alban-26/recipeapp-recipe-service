package com.myapp.recipe.domain.service;


import com.my.common.api.Repository;
import com.my.common.api.UserId;
import com.myapp.recipe.domain.model.Ingredient;
import com.myapp.recipe.domain.model.Recipe;
import com.myapp.recipe.domain.model.RecipeId;
import com.myapp.recipe.domain.model.User;

import java.util.Collection;
import java.util.List;

public interface RecipeRepository extends Repository<Recipe, RecipeId> {

    //List<Recipe> findAllByUser(UserId userId);

    List<Ingredient> fetchIngredients(UserId userId);


    /**
     *
     select ingredient.name, ingredient.category from recipe join recipe_ingredient on recipe_ingredient.recipe_id = recipe.id
     join ingredient on ingredient.id = recipe_ingredient.ingredient_id
     where user_id = '52da2f6d-a9a9-43bc-a850-32bead7359cd'
     */

}

