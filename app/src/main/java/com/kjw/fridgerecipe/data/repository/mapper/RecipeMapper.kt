package com.kjw.fridgerecipe.data.repository.mapper

import com.kjw.fridgerecipe.data.local.entity.RecipeEntity
import com.kjw.fridgerecipe.data.local.entity.RecipeSearchMetadataEntity
import com.kjw.fridgerecipe.data.remote.RecipeDto
import com.kjw.fridgerecipe.data.remote.RecipeIngredientDto
import com.kjw.fridgerecipe.data.remote.RecipeStepDto
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.model.CookingToolType
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeSearchMetadata
import com.kjw.fridgerecipe.domain.model.RecipeStep

fun RecipeDto.toDomainModel(): Recipe {
    return Recipe(
        id = null,
        title = this.title ?: "제목 없음",
        servings = this.info?.servings ?: "-",
        time = this.info?.time ?: "-",
        level = LevelType.fromId(this.info?.level),
        ingredients = this.ingredients?.map { it.toDomainModel() } ?: emptyList(),
        steps = this.steps?.map { it.toDomainModel() } ?: emptyList()
    )
}

fun RecipeIngredientDto.toDomainModel(): RecipeIngredient {
    return RecipeIngredient(
        name = this.name ?: "재료",
        quantity = this.quantity ?: "-",
        isEssential = this.isEssential ?: false
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
        level = this.level.id,
        ingredients = this.ingredients,
        steps = this.steps,
        imageUri = this.imageUri,
        searchMetadata = this.searchMetadata?.toEntity()
    )
}

fun RecipeSearchMetadata.toEntity(): RecipeSearchMetadataEntity {
    return RecipeSearchMetadataEntity(
        ingredientsQuery = this.ingredientsQuery,
        timeFilter = this.timeFilter,
        levelFilter = this.levelFilter?.id,
        categoryFilter = this.categoryFilter?.id,
        cookingToolFilter = this.cookingToolFilter?.id,
        useOnlySelected = this.useOnlySelected
    )
}

fun RecipeEntity.toDomainModel(): Recipe {
    return Recipe(
        id = this.id,
        title = this.title,
        servings = this.servings,
        time = this.time,
        level = LevelType.fromId(this.level),
        ingredients = this.ingredients,
        steps = this.steps,
        imageUri = this.imageUri,
        searchMetadata = this.searchMetadata?.toDomainModel()
    )
}

fun RecipeSearchMetadataEntity.toDomainModel(): RecipeSearchMetadata {
    return RecipeSearchMetadata(
        ingredientsQuery = this.ingredientsQuery,
        timeFilter = this.timeFilter,
        levelFilter = LevelType.fromId(this.levelFilter),
        categoryFilter = RecipeCategoryType.fromId(this.categoryFilter),
        cookingToolFilter = CookingToolType.fromId(this.cookingToolFilter),
        useOnlySelected = this.useOnlySelected
    )
}
