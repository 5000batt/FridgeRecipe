package com.kjw.fridgerecipe.presentation.ui.model

import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.model.CookingToolType
import com.kjw.fridgerecipe.presentation.util.UiText

data class ErrorDialogState(
    val title: UiText,
    val message: UiText
)

data class FilterOption<T>(
    val value: T,
    val label: UiText
)

data class RecipeFilterState(
    val timeLimit: String? = null,
    val level: LevelType? = null,
    val category: RecipeCategoryType? = null,
    val cookingTool: CookingToolType? = null,
    val useOnlySelected: Boolean = false
)