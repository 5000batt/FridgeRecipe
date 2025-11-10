package com.kjw.fridgerecipe.domain.repository

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    suspend fun getAiRecipes(
        ingredients: List<Ingredient>,
        ingredientsQuery: String,
        timeFilter: String?,
        levelFilter: LevelType?,
        categoryFilter: String?,
        utensilFilter: String?,
        useOnlySelected: Boolean
    ): Recipe?

    fun getAllSavedRecipes(): Flow<List<Recipe>>

    suspend fun getSavedRecipeById(id: Long): Recipe?

    suspend fun findRecipesByFilters(
        ingredientsQuery: String,
        timeFilter: String?,
        levelFilter: LevelType?,
        categoryFilter: String?,
        utensilFilter: String?,
        useOnlySelected: Boolean
    ): List<Recipe>

    suspend fun insertRecipe(recipe: Recipe)

    suspend fun updateRecipe(recipe: Recipe)

    suspend fun deleteRecipe(recipe: Recipe)
}