package com.myapp.recipe.adapter.database;

import com.my.common.api.pagination.PageRequest;
import com.myapp.recipe.domain.model.Tag;

import java.util.Set;

public interface RecipePageRequest extends PageRequest {

    Set<String> getTags();

}