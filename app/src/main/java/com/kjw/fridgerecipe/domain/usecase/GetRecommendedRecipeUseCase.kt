package com.kjw.fridgerecipe.domain.usecase

import android.util.Log
import com.kjw.fridgerecipe.domain.model.CookingToolType
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import com.kjw.fridgerecipe.domain.util.DataResult
import javax.inject.Inject

class GetRecommendedRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(
        ingredients: List<Ingredient>,
        seenIds: Set<Long>,
        timeFilter: String?,
        level: LevelType?,
        categoryFilter: RecipeCategoryType?,
        cookingToolFilter: CookingToolType?,
        useOnlySelected: Boolean,
        excludedIngredients: List<String> = emptyList(),
        onAiCall: suspend () -> Unit
    ): DataResult<Recipe> {

        val ingredientsQuery = ingredients
            .map { it.name }
            .sorted()
            .joinToString(",")

        // 1. 캐시 확인
        val cachedResult = recipeRepository.findRecipesByFilters(
            ingredientsQuery = ingredientsQuery,
            timeFilter = timeFilter,
            level = level,
            categoryFilter = categoryFilter,
            cookingToolFilter = cookingToolFilter,
            useOnlySelected = useOnlySelected
        )

        val cachedList = cachedResult.getOrNull() ?: emptyList()

        if (cachedList.isNotEmpty()) {
            val availableCache = cachedList.filter { recipe ->
                val isSeen = recipe.id in seenIds

                val hasExcludedIngredient = if (excludedIngredients.isNotEmpty()) {
                    recipe.ingredients.any { ingredient ->
                        excludedIngredients.any { excluded ->
                            ingredient.name.contains(excluded, ignoreCase = true)
                        }
                    }
                } else {
                    false
                }

                !isSeen && !hasExcludedIngredient
            }

            if (availableCache.isNotEmpty()) {
                Log.d("RecipeUseCase", "캐시된 목록 (다른 레시피) 반환")
                return DataResult.Success(availableCache.random())
            }
        }

        // 2. AI 호출 (캐시가 없거나 모두 순회했을 때)
        Log.d("RecipeUseCase", "AI 호출 (캐시 없음 또는 모두 순회)")
        onAiCall()

        return recipeRepository.getAiRecipes(
            ingredients = ingredients,
            ingredientsQuery = ingredientsQuery,
            timeFilter = timeFilter,
            level = level,
            categoryFilter = categoryFilter,
            cookingToolFilter = cookingToolFilter,
            useOnlySelected = useOnlySelected,
            excludedIngredients = excludedIngredients
        )
    }
}
