package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.data.repository.TicketRepository
import com.kjw.fridgerecipe.domain.model.CookingToolType
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import com.kjw.fridgerecipe.domain.util.DataError
import com.kjw.fridgerecipe.domain.util.DataResult
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class RecommendedRecipeResult(
    val recipe: Recipe,
    val isFromCache: Boolean,
)

class GetRecommendedRecipeUseCase
    @Inject
    constructor(
        private val recipeRepository: RecipeRepository,
        private val ticketRepository: TicketRepository,
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
        ): DataResult<RecommendedRecipeResult> {
            val ingredientsQuery =
                ingredients
                    .map { it.name }
                    .sorted()
                    .joinToString(",")

            // 1. 캐시 확인
            val cachedResult =
                recipeRepository.findRecipesByFilters(
                    ingredientsQuery = ingredientsQuery,
                    timeFilter = timeFilter,
                    level = level,
                    categoryFilter = categoryFilter,
                    cookingToolFilter = cookingToolFilter,
                    useOnlySelected = useOnlySelected,
                )

            val cachedList = cachedResult.getOrNull() ?: emptyList()

            if (cachedList.isNotEmpty()) {
                val availableCache =
                    cachedList.filter { recipe ->
                        val isSeen = recipe.id in seenIds

                        val hasExcludedIngredient =
                            if (excludedIngredients.isNotEmpty()) {
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
                    return DataResult.Success(RecommendedRecipeResult(recipe = availableCache.random(), isFromCache = true))
                }
            }

            // 2. 캐시가 없어서 AI를 호출해야 할 때 티켓 검사
            val currentTickets = ticketRepository.ticketCount.first()
            if (currentTickets <= 0) return DataResult.Error(DataError.TICKET_EXHAUSTED)

            // 3. 티켓 선 차감 후 AI 호출
            ticketRepository.useTicket()

            val aiResult =
                recipeRepository.getAiRecipes(
                    ingredients = ingredients,
                    ingredientsQuery = ingredientsQuery,
                    timeFilter = timeFilter,
                    level = level,
                    categoryFilter = categoryFilter,
                    cookingToolFilter = cookingToolFilter,
                    useOnlySelected = useOnlySelected,
                    excludedIngredients = excludedIngredients,
                )

            // 4. 결과 매핑 및 실패 시 복구
            return when (aiResult) {
                is DataResult.Success -> {
                    DataResult.Success(
                        RecommendedRecipeResult(recipe = aiResult.data, isFromCache = false),
                    )
                }
                is DataResult.Error -> {
                    ticketRepository.addTicket(1)
                    DataResult.Error(aiResult.error, aiResult.cause)
                }
            }
        }
    }
