package com.kjw.fridgerecipe.domain.repository

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.model.CookingToolType
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    suspend fun getAiRecipes(
        ingredients: List<Ingredient>,
        ingredientsQuery: String,
        timeFilter: String?,
        level: LevelType?,
        categoryFilter: RecipeCategoryType?,
        cookingToolFilter: CookingToolType?,
        useOnlySelected: Boolean,
        excludedIngredients: List<String> = emptyList()
    ): Recipe?

    fun getAllSavedRecipes(): Flow<List<Recipe>>

    suspend fun getSavedRecipeById(id: Long): Recipe?

    suspend fun findRecipesByFilters(
        ingredientsQuery: String,
        categoryFilter: RecipeCategoryType?,
        cookingToolFilter: CookingToolType?,
        timeFilter: String?,
        level: LevelType?,
        useOnlySelected: Boolean
    ): List<Recipe>

    suspend fun insertRecipe(recipe: Recipe)

    suspend fun updateRecipe(recipe: Recipe)

    suspend fun deleteRecipe(recipe: Recipe)

    suspend fun deleteAllRecipes()
}
