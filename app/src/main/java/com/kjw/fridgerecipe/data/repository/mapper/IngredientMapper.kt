package com.kjw.fridgerecipe.data.repository.mapper

import com.kjw.fridgerecipe.data.local.entity.IngredientEntity
import com.kjw.fridgerecipe.domain.model.CategoryType
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.IngredientIcon
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.domain.model.UnitType

fun IngredientEntity.toDomain(): Ingredient {
    return Ingredient(
        id = this.id,
        name = this.name,
        amount = this.amount,
        unit = UnitType.fromString(this.unitName),
        expirationDate = this.expirationDate,
        storageLocation = StorageType.fromString(this.storageLocationName),
        category = CategoryType.fromString(this.categoryName),
        emoticon = IngredientIcon.fromString(this.emoticonName)
    )
}

fun Ingredient.toEntity(): IngredientEntity {
    return IngredientEntity(
        id = this.id,
        name = this.name,
        amount = this.amount,
        unitName = this.unit.label,
        expirationDate = this.expirationDate,
        storageLocationName = this.storageLocation.label,
        categoryName = this.category.label,
        emoticonName = this.emoticon.label
    )
}