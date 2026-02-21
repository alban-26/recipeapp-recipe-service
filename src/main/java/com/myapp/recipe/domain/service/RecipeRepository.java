package com.myapp.recipe.domain.service;


import com.my.common.api.Repository;
import com.my.common.api.UserId;
import com.myapp.recipe.domain.model.Recipe;
import com.myapp.recipe.domain.model.RecipeId;

import java.util.List;

public interface RecipeRepository extends Repository<Recipe, RecipeId> {

    List<Recipe> findAllByUser(UserId userId);

}

