package com.myapp.recipe.domain.service;


import com.my.common.api.Repository;
import com.my.common.api.UserId;
import com.my.common.api.pagination.PagedRepository;
import com.myapp.recipe.adapter.database.RecipePageRequest;
import com.myapp.recipe.domain.model.Ingredient;
import com.myapp.recipe.domain.model.Recipe;
import com.myapp.recipe.domain.model.RecipeId;
import com.myapp.recipe.domain.model.Tag;


import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface RecipeRepository extends Repository<Recipe, RecipeId>, PagedRepository<Recipe, RecipePageRequest> {

    List<Ingredient> fetchIngredients(UserId userId);

    Set<Tag> fetchTags(UserId userId);

}

