package com.kjw.fridgerecipe.data.repository.mapper

import com.kjw.fridgerecipe.data.local.entity.IngredientEntity
import com.kjw.fridgerecipe.domain.model.CategoryType
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.IngredientIcon
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.domain.model.UnitType

fun <T : Enum<T>> safeValueOf(type: Class<T>, name: String, defaultValue: T): T {
    return try {
        java.lang.Enum.valueOf(type, name)
    } catch (e: Exception) {
        defaultValue
    }
}

fun IngredientEntity.toDomain(): Ingredient {
    return Ingredient(
        id = id,
        name = name,
        amount = amount,
        unit = safeValueOf(UnitType::class.java, unitName, UnitType.ETC),
        expirationDate = expirationDate,
        storageLocation = safeValueOf(StorageType::class.java, storageLocationName, StorageType.REFRIGERATED),
        category = safeValueOf(CategoryType::class.java, categoryName, CategoryType.ETC),
        emoticon = safeValueOf(IngredientIcon::class.java, emoticonName, IngredientIcon.DEFAULT)
    )
}

fun Ingredient.toEntity(): IngredientEntity {
    return IngredientEntity(
        id = id ?: 0L,
        name = name,
        amount = amount,
        unitName = unit.name,
        expirationDate = expirationDate,
        storageLocationName = storageLocation.name,
        categoryName = category.name,
        emoticonName = emoticon.name
    )
}