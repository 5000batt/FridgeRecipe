package com.kjw.fridgerecipe.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kjw.fridgerecipe.data.local.entity.RecipeEntity
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

    @Query("""
        SELECT * FROM recipes
        WHERE
            ingredientsQuery = :ingredientsQuery AND
            (:timeFilter IS NULL OR timeFilter = :timeFilter) AND
            (:levelFilter IS NULL OR levelFilter = :levelFilter) AND
            (:categoryFilter IS NULL OR categoryFilter = :categoryFilter) AND
            (:utensilFilter IS NULL OR utensilFilter = :utensilFilter) AND
            (useOnlySelected = :useOnlySelected)
    """)
    suspend fun findRecipesByFilters(
        ingredientsQuery: String,
        timeFilter: String?,
        levelFilter: String?,
        categoryFilter: String?,
        utensilFilter: String?,
        useOnlySelected: Boolean
    ): List<RecipeEntity>
}