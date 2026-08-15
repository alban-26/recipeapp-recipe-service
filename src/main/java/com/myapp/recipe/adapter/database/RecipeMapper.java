package com.myapp.recipe.adapter.database;

import com.myapp.recipe.adapter.database.entities.*;
import com.myapp.recipe.adapter.database.typehandler.DurationTypeHandler;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

@Mapper
public interface RecipeMapper {

    // ================================
    // Recipe
    // ================================
    @Insert("INSERT INTO recipe (name, portions, duration, user_id) " +
            "VALUES (#{name}, #{portions}, #{duration, typeHandler=com.myapp.recipe.adapter.database.typehandler.DurationTypeHandler}, #{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertRecipe(RecipeEntity entity);

    @Update("UPDATE recipe SET name = #{name}, portions = #{portions}, " +
            "duration = #{duration, typeHandler=com.myapp.recipe.adapter.database.typehandler.DurationTypeHandler} " +
            "WHERE id = #{id}")
    void updateRecipe(RecipeEntity entity);

    @Delete("DELETE FROM recipe WHERE id = #{id}")
    void deleteRecipe(Long id);


    // ================================
    // Ingredient dictionary
    // ================================
    @Insert("""
                INSERT INTO ingredient (name, category)
                VALUES (#{name}, #{category})
                ON CONFLICT (name) DO NOTHING
                RETURNING id
            """)
    Long insertIngredient(IngredientEntity entity);


    @Select("SELECT id, name, category FROM ingredient WHERE id = #{id}")
    IngredientEntity selectIngredientById(Long id);

    @Select("select distinct tag.name from tag join recipe_tag on tag.id = recipe_tag.tag_id join recipe on recipe_tag.recipe_id = recipe.id " +
            "where recipe.user_id = #{userId}")
    List<String> selectTagsByUser(String userId);

    @Select("SELECT id, name, category FROM ingredient")
    List<IngredientEntity> selectAllIngredients();

    @Select("select ingredient.name, ingredient.category from recipe join recipe_ingredient on recipe_ingredient.recipe_id = recipe.id " +
            "join ingredient on ingredient.id = recipe_ingredient.ingredient_id " +
            "where user_id = #{userId}")
    List<IngredientEntity> selectIngredientsByUserId(String userId);


    // ================================
    // Recipe ↔ Ingredient link
    // ================================
    @Insert("INSERT INTO recipe_ingredient (recipe_id, ingredient_id, total_quantity, unit) " +
            "VALUES (#{recipeId}, #{ingredientId}, #{totalQuantity}, #{unit})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertRecipeIngredient(RecipeIngredientEntity entity);

    @Select("SELECT ri.id AS recipeIngredientId, ri.recipe_id AS recipeId, " +
            "ri.ingredient_id AS ingredientId, i.name AS ingredientName, i.category AS ingredientCategory, " +
            "ri.total_quantity AS totalQuantity, ri.unit AS unit " +
            "FROM recipe_ingredient ri " +
            "JOIN ingredient i ON ri.ingredient_id = i.id " +
            "WHERE ri.recipe_id = #{recipeId}")
    @Results({
            @Result(property = "id", column = "recipeIngredientId"),
            @Result(property = "recipeId", column = "recipeId"),
            @Result(property = "ingredientId", column = "ingredientId"),
            @Result(property = "ingredientName", column = "ingredientName"),
            @Result(property = "ingredientCategory", column = "ingredientCategory"),
            @Result(property = "totalQuantity", column = "totalQuantity"),
            @Result(property = "unit", column = "unit")
    })
    List<RecipeIngredientEntity> selectRecipeIngredients(Long recipeId);

    @Delete("DELETE FROM recipe_ingredient WHERE recipe_id = #{recipeId}")
    void deleteRecipeIngredientsByRecipeId(Long recipeId);


    @Select("SELECT id FROM ingredient WHERE name = #{name}")
    Long findByName(@Param("name") String name);

    @Select("SELECT id FROM tag WHERE name = #{name}")
    Long findTagByName(@Param("name") String name);


    // ================================
    // Cooking Instructions
    // ================================
    @Insert("INSERT INTO cooking_instruction (recipe_id, step_number, instruction) " +
            "VALUES (#{recipeId}, #{stepNumber}, #{instruction})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertCookingInstruction(CookingInstructionEntity entity);

    @Select("SELECT id AS cookingInstructionId, recipe_id AS recipeId, step_number AS stepNumber, instruction " +
            "FROM cooking_instruction WHERE recipe_id = #{recipeId} ORDER BY step_number ASC")
    @Results({
            @Result(property = "id", column = "cookingInstructionId"),
            @Result(property = "recipeId", column = "recipeId"),
            @Result(property = "stepNumber", column = "stepNumber"),
            @Result(property = "instruction", column = "instruction"),
            @Result(property = "instructionIngredients", javaType = List.class, column = "cookingInstructionId",
                    many = @Many(select = "selectInstructionIngredients"))
    })
    List<CookingInstructionEntity> selectCookingInstructions(Long recipeId);

    @Delete("DELETE FROM cooking_instruction WHERE recipe_id = #{recipeId}")
    void deleteCookingInstructionsByRecipeId(Long recipeId);


    // ================================
    // Instruction ↔ Ingredient usage
    // ================================
    @Insert("INSERT INTO instruction_ingredient (cooking_instruction_id, recipe_ingredient_id, quantity) " +
            "VALUES (#{cookingInstructionId}, #{recipeIngredientId}, #{quantity})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertInstructionIngredient(InstructionIngredientEntity entity);

    @Select("SELECT ii.id AS instructionIngredientId, ii.cooking_instruction_id AS cookingInstructionId, " +
            "ii.recipe_ingredient_id AS recipeIngredientId, ri.total_quantity AS totalQuantity, " +
            "ii.quantity AS quantity, i.name AS ingredientName, i.category AS ingredientCategory, ri.unit AS unit " +
            "FROM instruction_ingredient ii " +
            "JOIN recipe_ingredient ri ON ii.recipe_ingredient_id = ri.id " +
            "JOIN ingredient i ON ri.ingredient_id = i.id " +
            "WHERE ii.cooking_instruction_id = #{cookingInstructionId}")
    @Results({
            @Result(property = "id", column = "instructionIngredientId"),
            @Result(property = "cookingInstructionId", column = "cookingInstructionId"),
            @Result(property = "recipeIngredientId", column = "recipeIngredientId"),
            @Result(property = "ingredientName", column = "ingredientName"),
            @Result(property = "ingredientCategory", column = "ingredientCategory"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "unit", column = "unit"),
            @Result(property = "totalQuantity", column = "totalQuantity")
    })
    List<InstructionIngredientEntity> selectInstructionIngredients(Long cookingInstructionId);

    @Delete("DELETE FROM instruction_ingredient WHERE cooking_instruction_id IN (" +
            "SELECT id FROM cooking_instruction WHERE recipe_id = #{recipeId})")
    void deleteInstructionIngredientsByRecipeId(Long recipeId);


    // ================================
    // Fetch full Recipe with nested children
    // ================================
    @Select("SELECT id AS recipeId, name, portions, duration, user_id FROM recipe WHERE id = #{recipeId}")
    @Results({
            @Result(property = "id", column = "recipeId"),
            @Result(property = "name", column = "name"),
            @Result(property = "portions", column = "portions"),
            @Result(property = "duration", column = "duration", typeHandler = DurationTypeHandler.class),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "recipeIngredients", javaType = List.class, column = "recipeId",
                    many = @Many(select = "selectRecipeIngredients")),
            @Result(property = "cookingInstructions", javaType = List.class, column = "recipeId",
                    many = @Many(select = "selectCookingInstructions")),
            @Result(property = "tags", javaType = List.class, column = "recipeId",
                    many = @Many(select = "selectTagsByRecipeId"))
    })
    RecipeEntity selectRecipe(Long recipeId);

    @Select("SELECT id AS recipeId, name, portions, duration, user_id FROM recipe")
    @Results({
            @Result(property = "id", column = "recipeId"),
            @Result(property = "name", column = "name"),
            @Result(property = "portions", column = "portions"),
            @Result(property = "duration", column = "duration", typeHandler = DurationTypeHandler.class),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "recipeIngredients", javaType = List.class, column = "recipeId",
                    many = @Many(select = "selectRecipeIngredients")),
            @Result(property = "cookingInstructions", javaType = List.class, column = "recipeId",
                    many = @Many(select = "selectCookingInstructions")),
            @Result(property = "tags", javaType = List.class, column = "recipeId",
                    many = @Many(select = "selectTagsByRecipeId"))
    })
    List<RecipeEntity> selectRecipes();

    @Select("SELECT id AS recipeId, name, portions, duration, user_id FROM recipe WHERE user_id = #{userId}")
    @Results({
            @Result(property = "id", column = "recipeId"),
            @Result(property = "name", column = "name"),
            @Result(property = "portions", column = "portions"),
            @Result(property = "duration", column = "duration", typeHandler = DurationTypeHandler.class),
            @Result(property = "userId", column = "user_Id"),
            @Result(property = "recipeIngredients", javaType = List.class, column = "recipeId",
                    many = @Many(select = "selectRecipeIngredients")),
            @Result(property = "cookingInstructions", javaType = List.class, column = "recipeId",
                    many = @Many(select = "selectCookingInstructions")),
            @Result(property = "tags", javaType = List.class, column = "recipeId",
                    many = @Many(select = "selectTagsByRecipeId"))
    })
    List<RecipeEntity> selectRecipesByUser(String userId);

    @Select("""
            <script>
            SELECT r.id AS recipeId,
                   r.name,
                   r.portions,
                   r.duration,
                   r.user_id
            FROM recipe r
            WHERE r.user_id = #{userId}
            <if test="searchQuery != null and searchQuery != ''">
                AND r.name ILIKE CONCAT('%', #{searchQuery}, '%')
            </if>
                    <if test="tags != null and tags.size() > 0">
                        AND (
                            SELECT COUNT(DISTINCT t.name)
                            FROM recipe_tag rt
                            JOIN tag t ON t.id = rt.tag_id
                            WHERE rt.recipe_id = r.id
                              AND t.name IN
                              <foreach item="tag" collection="tags" open="(" separator="," close=")">
                                  #{tag}
                              </foreach>
                        ) = #{tagCount}
                    </if>
            ORDER BY r.name ASC
            LIMIT #{limit}
            OFFSET #{offset}
            </script>
            """)
    @Results({
            @Result(property = "id", column = "recipeId"),
            @Result(property = "name", column = "name"),
            @Result(property = "portions", column = "portions"),
            @Result(property = "duration", column = "duration", typeHandler = DurationTypeHandler.class),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "recipeIngredients", javaType = List.class, column = "recipeId",
                    many = @Many(select = "selectRecipeIngredients")),
            @Result(property = "cookingInstructions", javaType = List.class, column = "recipeId",
                    many = @Many(select = "selectCookingInstructions")),
            @Result(property = "tags", javaType = List.class, column = "recipeId",
                    many = @Many(select = "selectTagsByRecipeId"))
    })
    List<RecipeEntity> selectRecipesByUserPaged(
            @Param("userId") String userId,
            @Param("limit") int limit,
            @Param("offset") int offset,
            @Param("searchQuery") String searchQuery,
            @Param("tags") List<String> tags,
            @Param("tagCount") int tagCount
    );


    @Select("""
        <script>
        SELECT COUNT(*)
        FROM recipe r
        WHERE r.user_id = #{userId}
        <if test="searchQuery != null and searchQuery != ''">
            AND r.name ILIKE CONCAT('%', #{searchQuery}, '%')
        </if>
        <if test="tags != null and tags.size() > 0">
            AND (
                SELECT COUNT(DISTINCT t.name)
                FROM recipe_tag rt
                JOIN tag t ON t.id = rt.tag_id
                WHERE rt.recipe_id = r.id
                  AND t.name IN
                  <foreach item="tag" collection="tags" open="(" separator="," close=")">
                      #{tag}
                  </foreach>
            ) = #{tagCount}
        </if>
        </script>
        """)
    long countRecipesByUser(
            @Param("userId") String userId,
            @Param("searchQuery") String searchQuery,
            @Param("tags") List<String> tags,
            @Param("tagCount") int tagCount);


    // ================================
    // Tags
    // ================================
    @Insert("INSERT INTO tag (name) VALUES (#{name}) ON CONFLICT (name) DO NOTHING")
    void insertTag(TagEntity tag);

    @Select("SELECT id FROM tag WHERE name = #{name}")
    Long findTagIdByName(@Param("name") String name);

    @Insert("INSERT INTO recipe_tag (recipe_id, tag_id) " +
            "VALUES (#{recipeId}, #{tagId}) ON CONFLICT DO NOTHING")
    void insertRecipeTag(RecipeTagEntity recipeTagEntity);

    @Select("SELECT t.name AS tagName FROM recipe_tag rt " +
            "JOIN tag t ON t.id = rt.tag_id " +
            "WHERE rt.recipe_id = #{recipeId}")
    @Results({
            @Result(property = "tagName", column = "tagName")
    })
    List<RecipeTagEntity> selectTagsByRecipeId(Long recipeId);

    @Delete("DELETE FROM recipe_tag WHERE recipe_id = #{recipeId}")
    void deleteRecipeTagsByRecipeId(Long recipeId);

}
