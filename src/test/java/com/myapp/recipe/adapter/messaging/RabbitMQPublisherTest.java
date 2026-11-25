package com.myapp.recipe.adapter.messaging;

import com.my.common.api.UserId;
import com.myapp.recipe.EmbeddedPostgresResource;
import com.myapp.recipe.domain.model.Ingredient;
import com.myapp.recipe.domain.model.ProductCategory;
import com.myapp.recipe.domain.model.RecipeId;
import com.myapp.recipe.domain.model.RecipeInfo;
import com.myapp.recipe.domain.model.RecipeIngredient;
import com.myapp.recipe.domain.model.Unit;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.Json;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Was der recipe-Dienst an den Essensplan-Dienst meldet.
 *
 * Die JSON-Texte in {@link #WIRE_RENAMED} und {@link #WIRE_DELETED} stehen
 * wortgleich im Test RecipeSyncTest des meal-plan-Dienstes. Aendert sich das
 * Format hier, muss es dort mitgeaendert werden.
 */
@QuarkusTest
@QuarkusTestResource(EmbeddedPostgresResource.class)
class RabbitMQPublisherTest {

    static final String WIRE_RENAMED = "{\"id\":5,\"name\":\"Crepes\",\"portions\":2,"
            + "\"ingredients\":[{\"ingredient\":{\"name\":\"Mehl\",\"productCategory\":\"BAKERY\"},\"quantity\":250.0,\"unit\":\"GRAM\"}],"
            + "\"userId\":{\"value\":\"user-1\"},\"previousName\":\"Pfannkuchen\",\"deleted\":false}";

    static final String WIRE_DELETED = "{\"id\":5,\"name\":\"Crepes\",\"portions\":2,"
            + "\"ingredients\":[{\"ingredient\":{\"name\":\"Mehl\",\"productCategory\":\"BAKERY\"},\"quantity\":250.0,\"unit\":\"GRAM\"}],"
            + "\"userId\":{\"value\":\"user-1\"},\"previousName\":null,\"deleted\":true}";

    private static RecipeInfo recipe(String name) {
        return new RecipeInfo(
                new RecipeId(5),
                name,
                List.of(new RecipeIngredient(new Ingredient("Mehl", ProductCategory.BAKERY), 250, Unit.GRAM)),
                2,
                new UserId("user-1"));
    }

    private Emitter<RecipeCreatedEvent> emitter;
    private RabbitMQPublisher publisher;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        emitter = Mockito.mock(Emitter.class);
        publisher = new RabbitMQPublisher(null);
        publisher.emitter = emitter;
    }

    private RecipeCreatedEvent sent() {
        ArgumentCaptor<RecipeCreatedEvent> event = ArgumentCaptor.forClass(RecipeCreatedEvent.class);
        Mockito.verify(emitter).send(event.capture());
        return event.getValue();
    }

    @Test
    void newRecipe() {
        publisher.publishRecipeInfo(recipe("Pfannkuchen"));

        RecipeCreatedEvent event = sent();
        assertEquals(5L, event.getId());
        assertEquals("Pfannkuchen", event.getName());
        assertEquals(2, event.getPortions());
        assertEquals(1, event.getIngredients().size());
        assertEquals("user-1", event.getUserId().value());
        assertNull(event.getPreviousName());
        assertFalse(event.isDeleted());
    }

    @Test
    void changedRecipeWithSameName() {
        publisher.publishRecipeUpdated(recipe("Pfannkuchen"), "Pfannkuchen");

        RecipeCreatedEvent event = sent();
        assertEquals("Pfannkuchen", event.getName());
        assertNull(event.getPreviousName(), "Kein Vorname, wenn sich der Name nicht geaendert hat");
        assertFalse(event.isDeleted());
    }

    @Test
    void renamedRecipe() {
        publisher.publishRecipeUpdated(recipe("Crepes"), "Pfannkuchen");

        RecipeCreatedEvent event = sent();
        assertEquals("Crepes", event.getName());
        assertEquals("Pfannkuchen", event.getPreviousName());
        assertFalse(event.isDeleted());
        // So geht die Meldung auf die Leitung (der Connector nutzt Json.encode).
        assertEquals(WIRE_RENAMED, Json.encode(event));
    }

    @Test
    void deletedRecipe() {
        publisher.publishRecipeDeleted(recipe("Crepes"));

        RecipeCreatedEvent event = sent();
        assertEquals(5L, event.getId());
        assertEquals("Crepes", event.getName());
        assertEquals("user-1", event.getUserId().value());
        assertTrue(event.isDeleted());
        assertEquals(WIRE_DELETED, Json.encode(event));
    }
}
