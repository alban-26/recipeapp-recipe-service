package com.myapp.recipe.application;

import com.my.common.api.UserId;
import com.myapp.recipe.EmbeddedPostgresResource;
import com.myapp.recipe.adapter.restapi.FileService;
import com.myapp.recipe.domain.model.CookingInstruction;
import com.myapp.recipe.domain.model.Ingredient;
import com.myapp.recipe.domain.model.ProductCategory;
import com.myapp.recipe.domain.model.Recipe;
import com.myapp.recipe.domain.model.RecipeId;
import com.myapp.recipe.domain.model.RecipeInfo;
import com.myapp.recipe.domain.model.RecipeIngredient;
import com.myapp.recipe.domain.model.Tag;
import com.myapp.recipe.domain.model.Unit;
import com.myapp.recipe.domain.service.MessagePublisher;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Anlegen, Aendern, Umbenennen und Loeschen eines Rezepts muessen gemeldet
 * werden - daraus haelt der Essensplan-Dienst seine Rezeptliste aktuell.
 */
@QuarkusTest
@QuarkusTestResource(EmbeddedPostgresResource.class)
class RecipeChangeEventsTest {

    private static final UserId USER = new UserId("events-user");

    @InjectMock
    MessagePublisher messagePublisher;

    @InjectMock
    FileService fileService;

    @Inject
    RecipeService recipeService;

    @BeforeEach
    @AfterEach
    void removeTestRecipes() {
        for (Recipe recipe : recipeService.getAllByUser(USER)) {
            recipeService.deleteById(recipe.id());
        }
        Mockito.clearInvocations(messagePublisher);
    }

    private static Recipe recipe(long id, String name, int portions) {
        Set<RecipeIngredient> ingredients = Set.of(
                new RecipeIngredient(new Ingredient("Mehl", ProductCategory.BAKERY), 250, Unit.GRAM));
        return new Recipe(
                new RecipeId(id),
                name,
                ingredients,
                List.of(new CookingInstruction("Alles verruehren.", ingredients)),
                portions,
                Duration.ofMinutes(20),
                new HashSet<>(List.of(new Tag("leicht"))),
                USER);
    }

    @Test
    void creatingIsReported() {
        Recipe created = recipeService.createRecipe(recipe(0, "Pfannkuchen", 2));

        ArgumentCaptor<RecipeInfo> info = ArgumentCaptor.forClass(RecipeInfo.class);
        Mockito.verify(messagePublisher).publishRecipeInfo(info.capture());
        assertEquals(created.id(), info.getValue().id());
        assertEquals("Pfannkuchen", info.getValue().name());
        assertEquals(USER, info.getValue().userId());
    }

    @Test
    void changingIsReportedWithThePreviousName() {
        Recipe created = recipeService.createRecipe(recipe(0, "Pfannkuchen", 2));
        Mockito.clearInvocations(messagePublisher);

        recipeService.updateRecipe(recipe(created.id().id(), "Pfannkuchen", 6));

        ArgumentCaptor<RecipeInfo> info = ArgumentCaptor.forClass(RecipeInfo.class);
        Mockito.verify(messagePublisher).publishRecipeUpdated(info.capture(), eq("Pfannkuchen"));
        assertEquals(created.id(), info.getValue().id());
        assertEquals(6, info.getValue().portions());
        Mockito.verify(messagePublisher, Mockito.never()).publishRecipeInfo(any());
    }

    @Test
    void renamingIsReportedWithThePreviousName() {
        Recipe created = recipeService.createRecipe(recipe(0, "Pfannkuchen", 2));
        Mockito.clearInvocations(messagePublisher);

        recipeService.updateRecipe(recipe(created.id().id(), "Crepes", 2));

        ArgumentCaptor<RecipeInfo> info = ArgumentCaptor.forClass(RecipeInfo.class);
        Mockito.verify(messagePublisher).publishRecipeUpdated(info.capture(), eq("Pfannkuchen"));
        assertEquals(created.id(), info.getValue().id());
        assertEquals("Crepes", info.getValue().name());
    }

    @Test
    void rejectedChangeIsNotReported() {
        recipeService.createRecipe(recipe(0, "Pfannkuchen", 2));
        Recipe waffles = recipeService.createRecipe(recipe(0, "Waffeln", 2));
        Mockito.clearInvocations(messagePublisher);

        assertThrows(RecipeAlreadyExistsException.class,
                () -> recipeService.updateRecipe(recipe(waffles.id().id(), "Pfannkuchen", 2)));

        Mockito.verify(messagePublisher, Mockito.never()).publishRecipeUpdated(any(), any());
    }

    @Test
    void deletingIsReported() {
        Recipe created = recipeService.createRecipe(recipe(0, "Pfannkuchen", 2));
        Mockito.clearInvocations(messagePublisher);

        recipeService.deleteById(created.id());

        ArgumentCaptor<RecipeInfo> info = ArgumentCaptor.forClass(RecipeInfo.class);
        Mockito.verify(messagePublisher).publishRecipeDeleted(info.capture());
        assertEquals(created.id(), info.getValue().id());
        assertEquals("Pfannkuchen", info.getValue().name());
        assertEquals(USER, info.getValue().userId());
    }

    @Test
    void deletingSucceedsEvenIfReportingFails() {
        Recipe created = recipeService.createRecipe(recipe(0, "Pfannkuchen", 2));
        Mockito.doThrow(new IllegalStateException("Broker nicht erreichbar"))
                .when(messagePublisher).publishRecipeDeleted(any());

        recipeService.deleteById(created.id());

        assertTrue(recipeService.getById(created.id()).isEmpty());
    }
}
