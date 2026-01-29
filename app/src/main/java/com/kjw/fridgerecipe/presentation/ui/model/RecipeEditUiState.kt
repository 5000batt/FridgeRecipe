package com.kjw.fridgerecipe.presentation.ui.model

import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.model.CookingToolType
import com.kjw.fridgerecipe.presentation.util.UiText

data class RecipeEditUiState(
    val title: String = "",
    val titleError: UiText? = null,
    val servingsState: String = "",
    val servingsError: UiText? = null,
    val timeState: String = "",
    val timeError: UiText? = null,
    val level: LevelType = LevelType.ETC,
    val categoryState: RecipeCategoryType? = null,
    val cookingToolState: CookingToolType? = null,
    val ingredientsState: List<IngredientItemUiState> = emptyList(),
    val ingredientsError: UiText? = null,
    val ingredientsErrorType: ListErrorType = ListErrorType.NONE,
    val stepsState: List<StepItemUiState> = emptyList(),
    val stepsError: UiText? = null,
    val stepsErrorType: ListErrorType = ListErrorType.NONE,
    val showDeleteDialog: Boolean = false,
    val imageUri: String? = null
)

data class IngredientItemUiState(
    val name: String,
    val quantity: String,
    val isEssential: Boolean
)

data class StepItemUiState(
    val number: Int,
    val description: String
)

enum class RecipeValidationField {
    TITLE, SERVINGS, TIME, INGREDIENTS, STEPS
}

enum class ListErrorType {
    NONE, IS_EMPTY, HAS_BLANK_ITEMS
}