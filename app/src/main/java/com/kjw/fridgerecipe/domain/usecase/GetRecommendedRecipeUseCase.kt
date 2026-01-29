package com.kjw.fridgerecipe.domain.usecase

import android.util.Log
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import javax.inject.Inject

class GetRecommendedRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(
        ingredients: List<Ingredient>,
        seenIds: Set<Long>,
        timeFilter: String?,
        levelFilter: LevelType?,
        categoryFilter: RecipeCategoryType?,
        utensilFilter: String?,
        useOnlySelected: Boolean,
        excludedIngredients: List<String> = emptyList(),
        onAiCall: suspend () -> Unit
    ): Recipe? {

        val ingredientsQuery = ingredients
            .map { it.name }
            .sorted()
            .joinToString(",")

        val cashedList = recipeRepository.findRecipesByFilters(
            ingredientsQuery = ingredientsQuery,
            timeFilter = timeFilter,
            levelFilter = levelFilter,
            categoryFilter = categoryFilter,
            utensilFilter = utensilFilter,
            useOnlySelected = useOnlySelected
        )

        if (cashedList.isNotEmpty()) {
            val availableCache = cashedList.filter { recipe ->
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
                return availableCache.random()
            }
        }

        Log.d("RecipeUseCase", "AI 호출 (캐시 없음 또는 모두 순회)")
        onAiCall()

        return recipeRepository.getAiRecipes(
            ingredients = ingredients,
            ingredientsQuery = ingredientsQuery,
            timeFilter = timeFilter,
            levelFilter = levelFilter,
            categoryFilter = categoryFilter,
            utensilFilter = utensilFilter,
            useOnlySelected = useOnlySelected,
            excludedIngredients = excludedIngredients
        )
    }
}