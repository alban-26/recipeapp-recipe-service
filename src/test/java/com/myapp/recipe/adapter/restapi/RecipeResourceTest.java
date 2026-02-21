package com.myapp.recipe.adapter.restapi;

import com.my.common.api.UserId;
import com.myapp.recipe.adapter.RecipeConverter;
import com.myapp.recipe.application.RecipeService;
import com.myapp.recipe.domain.model.Recipe;
import com.myapp.recipe.domain.model.RecipeId;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapitools.model.RecipeDto;

import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
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
        // Mock domain + DTO
        Recipe recipe = new Recipe(new RecipeId(1L), "Pasta", null, null, 0, null, new UserId("user1"));
        RecipeDto dto = new RecipeDto();
        dto.setId(1L);
        dto.setName("Pasta");

        Mockito.when(recipeService.getAllByUser(any())).thenReturn(List.of(recipe));
        Mockito.when(recipeConverter.domainToDto(recipe)).thenReturn(dto);

        given()
                .accept(ContentType.JSON)
                .when()
                .get("/recipes")
                .then()
                .statusCode(200)
                .body("[0].name", equalTo("Pasta"));
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void testGetRecipes_noContent() {
        Mockito.when(recipeService.getAllByUser(any())).thenReturn(List.of());

        given()
                .accept(ContentType.JSON)
                .when()
                .get("/recipes")
                .then()
                .statusCode(204);
    }

    @Test
    @TestSecurity(user = "user1", roles = {"user"})
    void testGetRecipeById_found() {
        Recipe recipe = new Recipe(new RecipeId(1L), "Cake", null, null, 0, null, new UserId("user1"));
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
        Recipe createdRecipe = new Recipe(new RecipeId(10L), "Soup", null, null, 0, null, new UserId("user1"));

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
        given()
                .when()
                .delete("/recipes/5")
                .then()
                .statusCode(200);
    }

}