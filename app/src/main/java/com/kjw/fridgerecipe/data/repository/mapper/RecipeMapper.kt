package com.kjw.fridgerecipe.data.repository.mapper

import com.kjw.fridgerecipe.data.local.entity.RecipeEntity
import com.kjw.fridgerecipe.data.remote.RecipeDto
import com.kjw.fridgerecipe.data.remote.RecipeIngredientDto
import com.kjw.fridgerecipe.data.remote.RecipeStepDto
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep

fun RecipeDto.toDomainModel(): Recipe {
    return Recipe(
        id = null,
        title = this.title ?: "제목 없음",
        servings = this.info?.servings ?: "-",
        time = this.info?.time ?: "-",
        level = LevelType.fromString(this.info?.level),
        ingredients = this.ingredients?.map { it.toDomainModel() } ?: emptyList(),
        steps = this.steps?.map { it.toDomainModel() } ?: emptyList()
    )
}

fun RecipeIngredientDto.toDomainModel(): RecipeIngredient {
    return RecipeIngredient(
        name = this.name ?: "재료",
        quantity = this.quantity ?: "-"
    )
}

fun RecipeStepDto.toDomainModel(): RecipeStep {
    return RecipeStep(
        number = this.number ?: 1,
        description = this.description ?: "설명 없음"
    )
}

fun Recipe.toEntity(): RecipeEntity {
    return RecipeEntity(
        id = this.id,
        title = this.title,
        servings = this.servings,
        time = this.time,
        level = this.level.label,
        ingredients = this.ingredients,
        steps = this.steps,
        ingredientsQuery = this.ingredientsQuery,
        timeFilter = this.timeFilter,
        levelFilter = this.levelFilter?.label,
        categoryFilter = this.categoryFilter,
        utensilFilter = this.utensilFilter,
        useOnlySelected = this.useOnlySelected
    )
}

fun RecipeEntity.toDomainModel(): Recipe {
    return Recipe(
        id = this.id,
        title = this.title,
        servings = this.servings,
        time = this.time,
        level = LevelType.fromString(this.level),
        ingredients = this.ingredients,
        steps = this.steps,
        ingredientsQuery = this.ingredientsQuery,
        timeFilter = this.timeFilter,
        levelFilter = this.levelFilter?.let { LevelType.fromString(it) },
        categoryFilter = this.categoryFilter,
        utensilFilter = this.utensilFilter,
        useOnlySelected = this.useOnlySelected
    )
}