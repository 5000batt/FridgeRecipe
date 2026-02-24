package com.kjw.fridgerecipe.presentation.mapper

import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep
import com.kjw.fridgerecipe.presentation.ui.model.IngredientItemUiState
import com.kjw.fridgerecipe.presentation.ui.model.RecipeEditUiState
import com.kjw.fridgerecipe.presentation.ui.model.StepItemUiState
import javax.inject.Inject

/**
 * 레시피 UI 관련 데이터 변환을 담당하는 매퍼 클래스
 */
class RecipeUiMapper
    @Inject
    constructor() {
        /**
         * Domain Model(Recipe)을 UI State(RecipeEditUiState)로 변환합니다.
         * @param recipe 변환할 도메인 모델
         * @return 변환된 UI 상태 객체
         */
        fun toEditUiState(recipe: Recipe): RecipeEditUiState =
            RecipeEditUiState(
                title = recipe.title,
                servingsState = if (recipe.servings > 0) recipe.servings.toString() else "",
                timeState = if (recipe.time > 0) recipe.time.toString() else "",
                level = recipe.level,
                categoryState = recipe.category,
                cookingToolState = recipe.cookingTool,
                ingredientsState =
                    recipe.ingredients.map {
                        IngredientItemUiState(
                            name = it.name,
                            quantity = it.quantity,
                            isEssential = it.isEssential,
                        )
                    },
                stepsState = recipe.steps.map { StepItemUiState(it.number, it.description) },
                imageUri = recipe.imageUri,
            )

        /**
         * UI State(RecipeEditUiState)를 Domain Model(Recipe)로 변환합니다.
         * @param state 현재 UI 상태
         * @param recipeId 수정 모드일 경우 기존 레시피의 ID
         * @return 변환된 도메인 모델 객체
         */
        fun toDomain(
            state: RecipeEditUiState,
            recipeId: Long?,
        ): Recipe {
            val actualTimeInt = state.timeState.toIntOrNull() ?: 0
            val actualServingsInt = state.servingsState.toIntOrNull() ?: 0

            val timeFilterTag =
                when {
                    actualTimeInt <= 15 -> "15분 이내"
                    actualTimeInt <= 30 -> "30분 이내"
                    actualTimeInt <= 60 -> "60분 이내"
                    else -> null
                }

            val ingredientsQueryTag =
                state.ingredientsState
                    .filter { it.isEssential }
                    .map { it.name }
                    .sorted()
                    .joinToString(",")

            return Recipe(
                id = recipeId,
                title = state.title.trim(),
                servings = actualServingsInt,
                time = actualTimeInt,
                level = state.level,
                ingredients =
                    state.ingredientsState.map {
                        RecipeIngredient(
                            name = it.name,
                            quantity = it.quantity,
                            isEssential = it.isEssential,
                        )
                    },
                steps = state.stepsState.map { RecipeStep(it.number, it.description) },
                category = state.categoryState,
                cookingTool = state.cookingToolState,
                timeFilter = timeFilterTag,
                ingredientsQuery = ingredientsQueryTag,
                useOnlySelected = false,
                imageUri = state.imageUri,
            )
        }
    }
