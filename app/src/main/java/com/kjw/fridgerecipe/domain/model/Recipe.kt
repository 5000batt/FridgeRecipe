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

data class RecipeSearchMetadata(
    val ingredientsQuery: String? = null,
    val timeFilter: String? = null,
    val levelFilter: LevelType? = null,
    val categoryFilter: RecipeCategoryType? = null,
    val cookingToolFilter: CookingToolType? = null,
    val useOnlySelected: Boolean = false
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

    // 검색 조건 메타데이터
    val searchMetadata: RecipeSearchMetadata? = null
)
