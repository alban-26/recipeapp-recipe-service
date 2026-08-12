package com.myapp.recipe.adapter.database;

import com.my.common.api.pagination.PageRequestImpl;
import com.myapp.recipe.domain.model.Tag;
import lombok.Getter;

import java.util.Set;

@Getter
public class RecipePageRequestImpl extends PageRequestImpl implements RecipePageRequest {



    private final Set<String> tags;

    public RecipePageRequestImpl(int page, int size, String searchQuery, Set<String> tags) {
        super(page, size, searchQuery);
        this.tags = tags;
    }


}