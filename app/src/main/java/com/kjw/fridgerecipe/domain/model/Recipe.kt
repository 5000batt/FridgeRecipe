package com.kjw.fridgerecipe.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RecipeIngredient(
    val name: String,
    val quantity: String,
    val isEssential: Boolean
)

@Serializable
data class RecipeStep(
    val number: Int,
    val description: String
)

data class Recipe(
    val id: Long? = null,
    val title: String,
    val servings: String,
    val time: String,
    val level: LevelType,
    val ingredients: List<RecipeIngredient>,
    val steps: List<RecipeStep>,
    val imageUri: String? = null,

    // 검색 조건 및 캐싱을 위한 정보
    val category: RecipeCategoryType? = null,
    val cookingTool: CookingToolType? = null,
    val timeFilter: String? = null,
    val ingredientsQuery: String? = null,
    val useOnlySelected: Boolean = false
)
