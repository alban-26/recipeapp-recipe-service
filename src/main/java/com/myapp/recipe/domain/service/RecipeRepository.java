package com.myapp.recipe.domain.service;


import com.my.common.api.Repository;
import com.my.common.api.UserId;
import com.my.common.api.pagination.PagedRepository;
import com.myapp.recipe.adapter.database.RecipePageRequest;
import com.myapp.recipe.domain.model.Ingredient;
import com.myapp.recipe.domain.model.Recipe;
import com.myapp.recipe.domain.model.RecipeId;


import java.util.Collection;
import java.util.List;

public interface RecipeRepository extends Repository<Recipe, RecipeId>, PagedRepository<Recipe, RecipePageRequest> {

    List<Ingredient> fetchIngredients(UserId userId);

}

