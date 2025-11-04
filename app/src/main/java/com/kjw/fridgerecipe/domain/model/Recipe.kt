package com.kjw.fridgerecipe.domain.model

data class RecipeIngredient(
    val name: String,
    val quantity: String
)

data class RecipeStep(
    val number: Int,
    val description: String
)

data class Recipe(
    val id: Long = 0L,
    val title: String,
    val servings: String,
    val time: String,
    val level: String,
    val ingredients: List<RecipeIngredient>,
    val steps: List<RecipeStep>,
    val ingredientsQuery: String = ""
)
