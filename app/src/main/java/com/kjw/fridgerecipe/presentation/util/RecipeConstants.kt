package com.kjw.fridgerecipe.presentation.util

import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.presentation.ui.model.FilterOption

object RecipeConstants {
    const val FILTER_ANY = "상관없음"

    val LEVEL_FILTER_OPTIONS = listOf(
        FilterOption(null, UiText.StringResource(R.string.filter_any)),
        FilterOption(LevelType.BEGINNER, UiText.StringResource(R.string.level_beginner)),
        FilterOption(LevelType.INTERMEDIATE, UiText.StringResource(R.string.level_intermediate)),
        FilterOption(LevelType.ADVANCED, UiText.StringResource(R.string.level_advanced))
    )

    val CATEGORY_FILTER_OPTIONS = listOf(
        FilterOption(FILTER_ANY, UiText.StringResource(R.string.filter_any)),
        FilterOption("한식", UiText.StringResource(R.string.category_korean)),
        FilterOption("일식", UiText.StringResource(R.string.category_japanese)),
        FilterOption("중식", UiText.StringResource(R.string.category_chinese)),
        FilterOption("양식", UiText.StringResource(R.string.category_western))
    )

    val UTENSIL_FILTER_OPTIONS = listOf(
        FilterOption(FILTER_ANY, UiText.StringResource(R.string.filter_any)),
        FilterOption("에어프라이어", UiText.StringResource(R.string.utensil_airfryer)),
        FilterOption("전자레인지", UiText.StringResource(R.string.utensil_microwave)),
        FilterOption("냄비", UiText.StringResource(R.string.utensil_pot)),
        FilterOption("후라이팬", UiText.StringResource(R.string.utensil_pan))
    )
}