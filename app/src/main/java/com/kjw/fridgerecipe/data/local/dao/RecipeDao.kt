package com.kjw.fridgerecipe.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kjw.fridgerecipe.data.local.entity.RecipeEntity
import com.kjw.fridgerecipe.domain.model.CookingToolType
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)

    @Query("SELECT * FROM recipes ORDER BY id DESC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Long): RecipeEntity?

    @Query("DELETE FROM recipes")
    suspend fun deleteAllRecipes()

    @Query(
        """
        SELECT * FROM recipes 
        WHERE ingredientsQuery = :ingredientsQuery 
        AND (:category IS NULL OR category = :category)
        AND (:cookingTool IS NULL OR cookingTool = :cookingTool)
        AND (:timeFilter IS NULL OR timeFilter = :timeFilter)
        AND (:level IS NULL OR level = :level)
        AND useOnlySelected = :useOnlySelected
    """,
    )
    suspend fun findRecipesByFilters(
        ingredientsQuery: String,
        category: RecipeCategoryType?,
        cookingTool: CookingToolType?,
        timeFilter: String?,
        level: LevelType?,
        useOnlySelected: Boolean,
    ): List<RecipeEntity>

    @Query("SELECT * FROM recipes WHERE title = :title AND ingredientsQuery = :ingredientsQuery LIMIT 1")
    suspend fun findExistingRecipeByTitle(
        title: String,
        ingredientsQuery: String,
    ): RecipeEntity?
}
