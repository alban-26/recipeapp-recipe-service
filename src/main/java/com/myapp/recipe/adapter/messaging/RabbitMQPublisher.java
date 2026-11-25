package com.myapp.recipe.adapter.messaging;

import com.myapp.recipe.adapter.RecipeConverter;
import com.myapp.recipe.domain.model.ProductCategory;
import com.myapp.recipe.domain.model.RecipeInfo;
import com.myapp.recipe.domain.model.RecipeIngredient;
import com.myapp.recipe.domain.service.MessagePublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;


@ApplicationScoped
public class RabbitMQPublisher implements MessagePublisher {

    private final RecipeConverter recipeConverter;

    @Inject
    @Channel("recipe-info")
    Emitter<RecipeCreatedEvent> emitter;

    @Inject
    public RabbitMQPublisher(RecipeConverter recipeConverter) {
        this.recipeConverter = recipeConverter;
    }

    @Override
    public void publishRecipeInfo(RecipeInfo info) {
        emitter.send(event(info, null, false));
    }

    @Override
    public void publishRecipeUpdated(RecipeInfo info, String previousName) {
        boolean renamed = previousName != null && !previousName.equals(info.name());
        emitter.send(event(info, renamed ? previousName : null, false));
    }

    @Override
    public void publishRecipeDeleted(RecipeInfo info) {
        // Zutaten und Portionen bleiben gefuellt: ein Empfaenger, der "deleted"
        // noch nicht kennt, behandelt die Meldung wie eine Aenderung und darf
        // dabei nichts verlieren.
        emitter.send(event(info, null, true));
    }

    private static RecipeCreatedEvent event(RecipeInfo info, String previousName, boolean deleted) {
        return new RecipeCreatedEvent(info.id().id(), info.name(), info.portions(), info.recipeIngredients(), info.userId(),
                previousName, deleted);
    }
}
