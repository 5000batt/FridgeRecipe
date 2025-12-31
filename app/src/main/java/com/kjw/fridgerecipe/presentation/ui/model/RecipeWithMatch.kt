package com.kjw.fridgerecipe.presentation.ui.model

import com.kjw.fridgerecipe.domain.model.Recipe

data class RecipeWithMatch(
    val recipe: Recipe,
    val matchCount: Int,
    val totalCount: Int,
    val matchPercentage: Int,
    val isCookable: Boolean,
    val missingIngredients: List<String>
)