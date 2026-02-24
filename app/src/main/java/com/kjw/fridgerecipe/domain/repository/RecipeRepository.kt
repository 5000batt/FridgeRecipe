package com.kjw.fridgerecipe.domain.repository

import com.kjw.fridgerecipe.domain.model.CookingToolType
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.util.DataResult
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    /**
     * AI로부터 레시피를 가져옵니다.
     */
    suspend fun getAiRecipes(
        ingredients: List<Ingredient>,
        ingredientsQuery: String,
        timeFilter: String?,
        level: LevelType?,
        categoryFilter: RecipeCategoryType?,
        cookingToolFilter: CookingToolType?,
        useOnlySelected: Boolean,
        excludedIngredients: List<String> = emptyList(),
    ): DataResult<Recipe>

    /**
     * 저장된 모든 레시피를 가져옵니다.
     */
    fun getAllSavedRecipes(): Flow<List<Recipe>>

    /**
     * 특정 ID의 저장된 레시피를 가져옵니다.
     */
    suspend fun getSavedRecipeById(id: Long): DataResult<Recipe>

    /**
     * 필터 조건에 맞는 저장된 레시피들을 검색합니다.
     */
    suspend fun findRecipesByFilters(
        ingredientsQuery: String,
        categoryFilter: RecipeCategoryType?,
        cookingToolFilter: CookingToolType?,
        timeFilter: String?,
        level: LevelType?,
        useOnlySelected: Boolean,
    ): DataResult<List<Recipe>>

    suspend fun insertRecipe(recipe: Recipe): DataResult<Long>

    suspend fun updateRecipe(recipe: Recipe): DataResult<Unit>

    suspend fun deleteRecipe(recipe: Recipe): DataResult<Unit>

    suspend fun deleteAllRecipes(): DataResult<Unit>
}
