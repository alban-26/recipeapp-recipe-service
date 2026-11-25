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

    /**
     * Name vor der Aenderung, wenn das Rezept umbenannt wurde - sonst null.
     * Damit findet der Essensplan-Dienst auch Eintraege wieder, die die ID
     * des Rezepts noch nicht kennen.
     */
    private String previousName;

    /** true: das Rezept wurde geloescht. */
    private boolean deleted;

}
