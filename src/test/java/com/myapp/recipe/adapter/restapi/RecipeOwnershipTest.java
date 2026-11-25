package com.myapp.recipe.adapter.restapi;

import com.my.common.api.UserId;
import com.myapp.recipe.EmbeddedPostgresResource;
import com.myapp.recipe.application.RecipeService;
import com.myapp.recipe.domain.model.Recipe;
import com.myapp.recipe.domain.model.RecipeId;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

/**
 * Prueft ueber die echte REST-Schnittstelle und die echte Datenbank, dass
 * Rezepte nur von ihrem Besitzer gelesen, geaendert und geloescht werden
 * koennen - und dass "Rezept bearbeiten" wirklich aendert statt neu anzulegen.
 */
@QuarkusTest
@QuarkusTestResource(EmbeddedPostgresResource.class)
class RecipeOwnershipTest {

    private static final String OWNER = "owner-a";
    private static final String STRANGER = "stranger-b";

    @InjectMock
    JsonWebToken jwt;

    @InjectMock
    FileService fileService;

    @Inject
    RecipeService recipeService;

    @BeforeEach
    @AfterEach
    void removeTestRecipes() {
        for (String user : List.of(OWNER, STRANGER)) {
            for (Recipe recipe : recipeService.getAllByUser(new UserId(user))) {
                recipeService.deleteById(recipe.id());
            }
        }
    }

    private void actAs(String userId) {
        Mockito.when(jwt.getSubject()).thenReturn(userId);
    }

    /** Derselbe Aufbau, den die App schickt. */
    private static Map<String, Object> recipeJson(long id, String name, int portions) {
        Map<String, Object> flour = Map.of("name", "Mehl", "category", "BAKERY", "quantity", 250.0, "unit", "g");
        Map<String, Object> milk = Map.of("name", "Milch", "category", "DAIRY", "quantity", 500.0, "unit", "ml");
        return Map.of(
                "id", id,
                "name", name,
                "recipeIngredients", List.of(flour, milk),
                "cookingInstructions", List.of(
                        Map.of("instruction", "Alles verruehren.", "recipeIngredients", List.of(flour, milk))),
                "portions", portions,
                "duration", "PT0H20M0S",
                "tags", List.of("leicht"));
    }

    private long createRecipe(String userId, String name) {
        actAs(userId);
        return Long.parseLong(given()
                .contentType(ContentType.JSON)
                .body(recipeJson(0, name, 2))
                .when()
                .post("/recipes")
                .then()
                .statusCode(201)
                .extract().asString().trim());
    }

    private Recipe stored(long id) {
        return recipeService.getById(new RecipeId(id)).orElseThrow();
    }

    // ------------------------------------------------------------------
    // Rezept bearbeiten
    // ------------------------------------------------------------------

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void updateWithUnchangedName_updatesInsteadOfConflict() {
        long id = createRecipe(OWNER, "Pfannkuchen");

        given()
                .contentType(ContentType.JSON)
                .body(recipeJson(id, "Pfannkuchen", 6))
                .when()
                .put("/recipes")
                .then()
                .statusCode(200)
                .body("id", equalTo((int) id))
                .body("name", equalTo("Pfannkuchen"))
                .body("portions", equalTo(6));

        Recipe recipe = stored(id);
        assertEquals(6, recipe.portions());
        assertEquals(2, recipe.recipeIngredients().size());
        assertEquals(1, recipe.cookingInstructions().size());
        assertEquals(1, recipeService.getAllByUser(new UserId(OWNER)).size(), "Es darf kein zweites Rezept entstehen");
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void updateWithNewName_renames() {
        long id = createRecipe(OWNER, "Pfannkuchen");

        given()
                .contentType(ContentType.JSON)
                .body(recipeJson(id, "Crepes", 2))
                .when()
                .put("/recipes")
                .then()
                .statusCode(200)
                .body("name", equalTo("Crepes"));

        assertEquals("Crepes", stored(id).name());
        assertEquals(1, recipeService.getAllByUser(new UserId(OWNER)).size());
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void updateToNameOfAnotherOwnRecipe_isConflict() {
        createRecipe(OWNER, "Pfannkuchen");
        long waffles = createRecipe(OWNER, "Waffeln");

        given()
                .contentType(ContentType.JSON)
                .body(recipeJson(waffles, "Pfannkuchen", 2))
                .when()
                .put("/recipes")
                .then()
                .statusCode(409)
                .body("code", equalTo("RECIPE_NAME_DUPLICATE"));

        assertEquals("Waffeln", stored(waffles).name());
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void updateOfUnknownRecipe_isNotFound() {
        actAs(OWNER);

        given()
                .contentType(ContentType.JSON)
                .body(recipeJson(987654321L, "Gibt es nicht", 2))
                .when()
                .put("/recipes")
                .then()
                .statusCode(404);

        assertTrue(recipeService.getAllByUser(new UserId(OWNER)).isEmpty(), "Ein Update darf nichts neu anlegen");
    }

    // ------------------------------------------------------------------
    // Fremde Rezepte
    // ------------------------------------------------------------------

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void ownerCanReadOwnRecipe() {
        long id = createRecipe(OWNER, "Pfannkuchen");

        given()
                .accept(ContentType.JSON)
                .when()
                .get("/recipes/" + id)
                .then()
                .statusCode(200)
                .body("name", equalTo("Pfannkuchen"));
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void strangerCannotReadRecipe() {
        long id = createRecipe(OWNER, "Pfannkuchen");

        actAs(STRANGER);
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/recipes/" + id)
                .then()
                .statusCode(404)
                .body("message", equalTo("Recipe not found"));
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void strangerCannotUpdateRecipe() {
        long id = createRecipe(OWNER, "Pfannkuchen");

        actAs(STRANGER);
        given()
                .contentType(ContentType.JSON)
                .body(recipeJson(id, "Gekapert", 99))
                .when()
                .put("/recipes")
                .then()
                .statusCode(404);

        Recipe recipe = stored(id);
        assertEquals("Pfannkuchen", recipe.name());
        assertEquals(2, recipe.portions());
        assertEquals(OWNER, recipe.userId().value());
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void strangerCannotOverwriteRecipeThroughCreate() {
        long id = createRecipe(OWNER, "Pfannkuchen");

        // Ein POST mit fremder ID darf das fremde Rezept nicht ueberschreiben,
        // sondern legt ein eigenes, neues Rezept an.
        actAs(STRANGER);
        long created = Long.parseLong(given()
                .contentType(ContentType.JSON)
                .body(recipeJson(id, "Gekapert", 99))
                .when()
                .post("/recipes")
                .then()
                .statusCode(201)
                .extract().asString().trim());

        assertTrue(created != id, "POST muss eine neue ID vergeben");
        Recipe original = stored(id);
        assertEquals("Pfannkuchen", original.name());
        assertEquals(2, original.portions());
        assertEquals(OWNER, original.userId().value());
        assertEquals(STRANGER, stored(created).userId().value());
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void strangerCannotDeleteRecipe() {
        long id = createRecipe(OWNER, "Pfannkuchen");

        actAs(STRANGER);
        given()
                .when()
                .delete("/recipes/" + id)
                .then()
                .statusCode(404);

        assertTrue(recipeService.getById(new RecipeId(id)).isPresent());
        Mockito.verify(fileService, Mockito.never()).deleteImage(any());
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void ownerCanDeleteOwnRecipe() {
        long id = createRecipe(OWNER, "Pfannkuchen");

        given()
                .when()
                .delete("/recipes/" + id)
                .then()
                .statusCode(200);

        assertTrue(recipeService.getById(new RecipeId(id)).isEmpty());
    }

    // ------------------------------------------------------------------
    // Rezeptbilder
    // ------------------------------------------------------------------

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void strangerCannotDownloadImage() throws IOException {
        long id = createRecipe(OWNER, "Pfannkuchen");
        File image = imageOnDisk();
        Mockito.when(fileService.getImage(any())).thenReturn(image);

        actAs(STRANGER);
        given()
                .when()
                .get("/recipes/" + id + "/image")
                .then()
                .statusCode(404);

        actAs(OWNER);
        given()
                .when()
                .get("/recipes/" + id + "/image")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void strangerCannotUploadImage() throws IOException {
        long id = createRecipe(OWNER, "Pfannkuchen");
        File image = imageOnDisk();

        actAs(STRANGER);
        given()
                .multiPart("image", image, "image/jpeg")
                .when()
                .post("/recipes/" + id + "/image")
                .then()
                .statusCode(404);
        Mockito.verify(fileService, Mockito.never()).saveImage(any(Path.class), any(InputStream.class));

        actAs(OWNER);
        given()
                .multiPart("image", image, "image/jpeg")
                .when()
                .post("/recipes/" + id + "/image")
                .then()
                .statusCode(200);
        Mockito.verify(fileService).saveImage(any(Path.class), any(InputStream.class));
    }

    private static File imageOnDisk() throws IOException {
        Path file = Files.createTempFile("recipe-image", ".jpg");
        Files.write(file, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xD9});
        file.toFile().deleteOnExit();
        return file.toFile();
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void listContainsOnlyOwnRecipes() {
        createRecipe(OWNER, "Pfannkuchen");
        createRecipe(STRANGER, "Waffeln");

        actAs(OWNER);
        Optional<String> foreign = given()
                .accept(ContentType.JSON)
                .when()
                .get("/recipes?page=0&size=50")
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("content.name", String.class).stream()
                .filter("Waffeln"::equals)
                .findAny();

        assertTrue(foreign.isEmpty());
    }
}
