package com.kjw.fridgerecipe.domain.model

data class RecipeWithMatch(
    val recipe: Recipe,
    val matchCount: Int,
    val totalCount: Int,
    val matchPercentage: Int,
    val isCookable: Boolean,
    val missingIngredients: List<String>
)
