package com.kjw.fridgerecipe.data.repository.mapper

import com.kjw.fridgerecipe.data.local.entity.IngredientEntity
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.IngredientCategoryType
import com.kjw.fridgerecipe.domain.model.IngredientIcon
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.domain.model.UnitType

fun Ingredient.toEntity(): IngredientEntity {
    return IngredientEntity(
        id = this.id,
        name = this.name,
        amount = this.amount,
        unitName = this.unit.id,
        expirationDate = this.expirationDate,
        storageLocationName = this.storageLocation.id,
        emoticonName = this.emoticon.id,
        categoryName = this.category.id
    )
}

fun IngredientEntity.toDomainModel(): Ingredient {
    return Ingredient(
        id = this.id,
        name = this.name,
        amount = this.amount,
        unit = UnitType.fromId(this.unitName),
        expirationDate = this.expirationDate,
        storageLocation = StorageType.fromId(this.storageLocationName),
        emoticon = IngredientIcon.fromId(this.emoticonName),
        category = IngredientCategoryType.fromId(this.categoryName)
    )
}