package com.kjw.fridgerecipe.presentation.util

import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.model.CookingToolType
import com.kjw.fridgerecipe.presentation.ui.model.FilterOption

object RecipeConstants {
    const val FILTER_ANY = "상관없음"

    val LEVEL_FILTER_OPTIONS = buildList {
        add(FilterOption(null, UiText.StringResource(R.string.filter_any)))
        addAll(
            LevelType.entries.filter { it != LevelType.ETC }.map { type ->
                FilterOption(
                    value = type,
                    label = UiText.StringResource(type.labelResId)
                )
            }
        )
    }

    val CATEGORY_FILTER_OPTIONS = buildList {
        add(FilterOption(null, UiText.StringResource(R.string.filter_any)))
        addAll(
            RecipeCategoryType.entries.map { type ->
                FilterOption(
                    value = type,
                    label = UiText.StringResource(type.labelResId)
                )
            }
        )
    }

    val COOKING_TOOL_FILTER_OPTIONS = buildList {
        add(FilterOption(null, UiText.StringResource(R.string.filter_any)))
        addAll(
            CookingToolType.entries.map { type ->
                FilterOption(
                    value = type,
                    label = UiText.StringResource(type.labelResId)
                )
            }
        )
    }
}
