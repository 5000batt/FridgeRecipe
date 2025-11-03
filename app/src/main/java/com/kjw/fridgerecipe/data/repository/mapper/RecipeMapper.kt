package com.kjw.fridgerecipe.data.repository.mapper

import com.kjw.fridgerecipe.data.remote.RecipeDto
import com.kjw.fridgerecipe.data.remote.RecipeIngredientDto
import com.kjw.fridgerecipe.data.remote.RecipeStepDto
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep

fun RecipeDto.toDomainModel(): Recipe {
    return Recipe(
        title = this.title ?: "제목 없음",
        servings = this.info?.servings ?: "-",
        time = this.info?.time ?: "-",
        level = this.info?.level ?: "-",
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