package com.myapp.recipe.adapter.messaging;


import com.my.common.api.UserId;
import com.myapp.recipe.domain.model.RecipeIngredient;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
public class RecipeCreatedEvent {

    private Long id;
    private String name;
    private Integer portions;
    private Collection<RecipeIngredient> ingredients;
    private UserId userId;

}
