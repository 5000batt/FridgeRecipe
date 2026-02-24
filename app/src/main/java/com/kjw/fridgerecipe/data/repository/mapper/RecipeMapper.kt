package com.kjw.fridgerecipe.data.repository.mapper

import com.kjw.fridgerecipe.data.local.entity.RecipeEntity
import com.kjw.fridgerecipe.data.remote.RecipeDto
import com.kjw.fridgerecipe.data.remote.RecipeIngredientDto
import com.kjw.fridgerecipe.data.remote.RecipeStepDto
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep

fun RecipeDto.toDomainModel(): Recipe =
    Recipe(
        id = null,
        title = this.title ?: "제목 없음",
        servings = extractNumber(this.info?.servings),
        time = extractNumber(this.info?.time),
        level = LevelType.fromId(this.info?.level),
        ingredients = this.ingredients?.map { it.toDomainModel() } ?: emptyList(),
        steps = this.steps?.map { it.toDomainModel() } ?: emptyList(),
    )

fun RecipeIngredientDto.toDomainModel(): RecipeIngredient =
    RecipeIngredient(
        name = this.name ?: "재료",
        quantity = this.quantity ?: "-",
        isEssential = this.isEssential ?: false,
    )

fun RecipeStepDto.toDomainModel(): RecipeStep =
    RecipeStep(
        number = this.number ?: 1,
        description = this.description ?: "설명 없음",
    )

fun Recipe.toEntity(): RecipeEntity =
    RecipeEntity(
        id = this.id,
        title = this.title,
        servings = this.servings,
        time = this.time,
        level = this.level,
        ingredients = this.ingredients,
        steps = this.steps,
        imageUri = this.imageUri,
        category = this.category,
        cookingTool = this.cookingTool,
        timeFilter = this.timeFilter,
        ingredientsQuery = this.ingredientsQuery,
        useOnlySelected = this.useOnlySelected,
    )

fun RecipeEntity.toDomainModel(): Recipe =
    Recipe(
        id = this.id,
        title = this.title,
        servings = this.servings,
        time = this.time,
        level = this.level,
        ingredients = this.ingredients,
        steps = this.steps,
        imageUri = this.imageUri,
        category = this.category,
        cookingTool = this.cookingTool,
        timeFilter = this.timeFilter,
        ingredientsQuery = this.ingredientsQuery,
        useOnlySelected = this.useOnlySelected,
    )

private fun extractNumber(text: String?): Int {
    if (text == null) return 0
    // 숫자만 추출 (예: "3~4인분" -> "34"가 될 수 있으므로 첫 번째 연속된 숫자 그룹만 가져옴)
    val match = Regex("\\d+").find(text)
    return match?.value?.toIntOrNull() ?: 0
}
