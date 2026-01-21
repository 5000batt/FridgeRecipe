package com.kjw.fridgerecipe.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AiRecipeResponse(
    val recipe: RecipeDto
)

@Serializable
data class RecipeIngredientDto(val name: String?, val quantity: String?, val isEssential: Boolean?)
@Serializable
data class RecipeStepDto(val number: Int?, val description: String?)
@Serializable
data class RecipeInfoDto(val servings: String?, val time: String?, val level: String?)
@Serializable
data class RecipeDto(
    val title: String?,
    val info: RecipeInfoDto?,
    val ingredients: List<RecipeIngredientDto>?,
    val steps: List<RecipeStepDto>?
)
