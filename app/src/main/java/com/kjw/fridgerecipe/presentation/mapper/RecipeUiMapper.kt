package com.kjw.fridgerecipe.presentation.mapper

import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep
import com.kjw.fridgerecipe.presentation.ui.model.IngredientItemUiState
import com.kjw.fridgerecipe.presentation.ui.model.RecipeEditUiState
import com.kjw.fridgerecipe.presentation.ui.model.StepItemUiState
import javax.inject.Inject

class RecipeUiMapper @Inject constructor() {

    fun toEditUiState(recipe: Recipe): RecipeEditUiState {
        val servingsExtracted = Regex("\\d+").find(recipe.servings)?.value ?: ""
        val timeExtracted = Regex("\\d+").find(recipe.time)?.value ?: ""

        return RecipeEditUiState(
            title = recipe.title,
            servingsState = servingsExtracted,
            timeState = timeExtracted,
            level = recipe.level,
            categoryState = recipe.category,
            cookingToolState = recipe.cookingTool,
            ingredientsState = recipe.ingredients.map {
                IngredientItemUiState(
                    name = it.name,
                    quantity = it.quantity,
                    isEssential = it.isEssential
                )
            },
            stepsState = recipe.steps.map { StepItemUiState(it.number, it.description) },
            imageUri = recipe.imageUri
        )
    }

    fun toDomain(state: RecipeEditUiState, recipeId: Long?): Recipe {
        val actualTimeInt = state.timeState.toIntOrNull() ?: 0
        
        val timeFilterTag = when {
            actualTimeInt <= 15 -> "15분 이내"
            actualTimeInt <= 30 -> "30분 이내"
            actualTimeInt <= 60 -> "60분 이내"
            else -> null
        }

        val ingredientsQueryTag = state.ingredientsState
            .filter { it.isEssential }
            .map { it.name }
            .sorted()
            .joinToString(",")

        return Recipe(
            id = recipeId,
            title = state.title.trim(),
            servings = "${state.servingsState}인분",
            time = "${state.timeState}분",
            level = state.level,
            ingredients = state.ingredientsState.map {
                RecipeIngredient(
                    name = it.name,
                    quantity = it.quantity,
                    isEssential = it.isEssential
                )
            },
            steps = state.stepsState.map { RecipeStep(it.number, it.description) },
            category = state.categoryState,
            cookingTool = state.cookingToolState,
            timeFilter = timeFilterTag,
            ingredientsQuery = ingredientsQueryTag,
            useOnlySelected = false,
            imageUri = state.imageUri
        )
    }
}
