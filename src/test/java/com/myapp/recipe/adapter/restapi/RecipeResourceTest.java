package com.myapp.recipe.adapter.restapi;

import com.my.common.api.UserId;
import com.my.common.api.pagination.PageResult;
import com.myapp.recipe.adapter.RecipeConverter;
import com.myapp.recipe.application.RecipeService;
import com.myapp.recipe.domain.model.Recipe;
import com.myapp.recipe.domain.model.RecipeId;
import com.myapp.recipe.domain.model.Tag;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapitools.model.RecipeDto;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
class RecipeResourceTest {


    @InjectMock
    RecipeService recipeService;

    @InjectMock
    RecipeConverter recipeConverter;

    @InjectMock
    JsonWebToken jwt;

    @InjectMock
    SecurityIdentity securityIdentity;

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void testGetRecipes_returnsList() {
        Recipe recipe = new Recipe(new RecipeId(1L), "Pasta", null, null, 0, null, new HashSet<>(List.of(new Tag("leicht"))),new UserId("user1"));
        RecipeDto dto = new RecipeDto();
        dto.setId(1L);
        dto.setName("Pasta");

        PageResult<Recipe> pageResult = new PageResult<>(
                List.of(recipe), // content
                1L,              // totalElements
                1,               // totalPages
                0,               // page
                20,              // size
                true             // last
        );

        Mockito.when(recipeService.getAllByUser(any(), any())).thenReturn(pageResult);
        Mockito.when(recipeConverter.domainToDto(recipe)).thenReturn(dto);

        given()
                .accept(ContentType.JSON)
                .when()
                .get("/recipes")
                .then()
                .statusCode(200)
                .body("content[0].name", equalTo("Pasta"));
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void testGetRecipes_noContent() {
        PageResult<Recipe> emptyPage = new PageResult<>(
                List.of(), // content
                0L,        // totalElements
                0,         // totalPages
                0,         // page
                20,        // size
                true        // last
        );

        Mockito.when(recipeService.getAllByUser(any(), any())).thenReturn(emptyPage);

        given()
                .accept(ContentType.JSON)
                .when()
                .get("/recipes")
                .then()
                .statusCode(200)
                .body("content", empty());
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void testGetRecipeById_found() {
        Mockito.when(jwt.getSubject()).thenReturn("user1");
        Recipe recipe = new Recipe(new RecipeId(1L), "Cake", null, null, 0, null, new HashSet<>(List.of(new Tag("leicht"))), new UserId("user1"));
        Mockito.when(recipeService.getById(any())).thenReturn(Optional.of(recipe));

        given()
                .accept(ContentType.JSON)
                .when()
                .get("/recipes/1")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void testGetRecipeById_foreignRecipe_notFound() {
        Mockito.when(jwt.getSubject()).thenReturn("user1");
        Recipe foreign = new Recipe(new RecipeId(1L), "Cake", null, null, 0, null, new HashSet<>(List.of(new Tag("leicht"))), new UserId("user2"));
        Mockito.when(recipeService.getById(any())).thenReturn(Optional.of(foreign));

        given()
                .accept(ContentType.JSON)
                .when()
                .get("/recipes/1")
                .then()
                .statusCode(404)
                .body("message", equalTo("Recipe not found"));
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void testGetRecipeById_notFound() {
        Mockito.when(recipeService.getById(any())).thenReturn(Optional.empty());

        given()
                .accept(ContentType.JSON)
                .when()
                .get("/recipes/9999")
                .then()
                .statusCode(404)
                .body("message", equalTo("Recipe not found"));
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void testCreateRecipe_createsSuccessfully() {
        RecipeDto dto = new RecipeDto();
        dto.setName("Soup");
        Recipe createdRecipe = new Recipe(new RecipeId(10L), "Soup", null, null, 0, null, new HashSet<>(List.of(new Tag("leicht"))), new UserId("user1"));

        Mockito.when(recipeConverter.dtoToDomain(any())).thenReturn(createdRecipe);
        Mockito.when(recipeService.createRecipe(any())).thenReturn(createdRecipe);

        given()
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .post("/recipes")
                .then()
                .statusCode(201)
                .header("Location", containsString("/recipes/10"));
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void testDeleteRecipe_returnsOk() {
        Mockito.when(jwt.getSubject()).thenReturn("user1");
        Recipe recipe = new Recipe(new RecipeId(5L), "Cake", null, null, 0, null, new HashSet<>(List.of(new Tag("leicht"))), new UserId("user1"));
        Mockito.when(recipeService.getById(new RecipeId(5L))).thenReturn(Optional.of(recipe));

        given()
                .when()
                .delete("/recipes/5")
                .then()
                .statusCode(200);

        Mockito.verify(recipeService).deleteById(new RecipeId(5L));
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void testDeleteRecipe_foreignRecipe_notFound() {
        Mockito.when(jwt.getSubject()).thenReturn("user1");
        Recipe foreign = new Recipe(new RecipeId(5L), "Cake", null, null, 0, null, new HashSet<>(List.of(new Tag("leicht"))), new UserId("user2"));
        Mockito.when(recipeService.getById(new RecipeId(5L))).thenReturn(Optional.of(foreign));

        given()
                .when()
                .delete("/recipes/5")
                .then()
                .statusCode(404);

        Mockito.verify(recipeService, Mockito.never()).deleteById(any());
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void testUpdateRecipe_updatesInsteadOfCreating() {
        Mockito.when(jwt.getSubject()).thenReturn("user1");
        RecipeDto dto = new RecipeDto();
        dto.setId(7L);
        dto.setName("Soup");
        Recipe recipe = new Recipe(new RecipeId(7L), "Soup", null, null, 0, null, new HashSet<>(List.of(new Tag("leicht"))), new UserId("user1"));

        Mockito.when(recipeConverter.dtoToDomain(any())).thenReturn(recipe);
        Mockito.when(recipeService.updateRecipe(any())).thenReturn(recipe);
        Mockito.when(recipeConverter.domainToDto(recipe)).thenReturn(dto);

        given()
                .contentType(ContentType.JSON)
                .body(dto)
                .when()
                .put("/recipes")
                .then()
                .statusCode(200)
                .body("id", equalTo(7))
                .body("name", equalTo("Soup"));

        Mockito.verify(recipeService).updateRecipe(recipe);
        Mockito.verify(recipeService, Mockito.never()).createRecipe(any());
    }

}