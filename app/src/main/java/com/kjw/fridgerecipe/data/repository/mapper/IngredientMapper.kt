package com.kjw.fridgerecipe.data.repository.mapper

import com.kjw.fridgerecipe.data.local.entity.IngredientEntity
import com.kjw.fridgerecipe.domain.model.Ingredient

fun Ingredient.toEntity(): IngredientEntity {
    return IngredientEntity(
        id = this.id,
        name = this.name,
        amount = this.amount,
        unit = this.unit,
        expirationDate = this.expirationDate,
        storageLocation = this.storageLocation,
        emoticon = this.emoticon,
        category = this.category
    )
}

fun IngredientEntity.toDomainModel(): Ingredient {
    return Ingredient(
        id = this.id,
        name = this.name,
        amount = this.amount,
        unit = this.unit,
        expirationDate = this.expirationDate,
        storageLocation = this.storageLocation,
        emoticon = this.emoticon,
        category = this.category
    )
}
