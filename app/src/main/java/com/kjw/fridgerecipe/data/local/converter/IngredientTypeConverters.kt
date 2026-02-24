package com.kjw.fridgerecipe.data.local.converter

import androidx.room.TypeConverter
import com.kjw.fridgerecipe.domain.model.IngredientCategoryType
import com.kjw.fridgerecipe.domain.model.IngredientIcon
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.domain.model.UnitType

class IngredientTypeConverters {
    @TypeConverter
    fun fromUnitType(value: UnitType?): String? = value?.name

    @TypeConverter
    fun toUnitType(value: String?): UnitType? =
        value?.let {
            try {
                UnitType.valueOf(it)
            } catch (e: Exception) {
                UnitType.COUNT
            }
        }

    @TypeConverter
    fun fromStorageType(value: StorageType?): String? = value?.name

    @TypeConverter
    fun toStorageType(value: String?): StorageType? =
        value?.let {
            try {
                StorageType.valueOf(it)
            } catch (e: Exception) {
                StorageType.REFRIGERATED
            }
        }

    @TypeConverter
    fun fromIngredientCategoryType(value: IngredientCategoryType?): String? = value?.name

    @TypeConverter
    fun toIngredientCategoryType(value: String?): IngredientCategoryType? =
        value?.let {
            try {
                IngredientCategoryType.valueOf(it)
            } catch (e: Exception) {
                IngredientCategoryType.VEGETABLE
            }
        }

    @TypeConverter
    fun fromIngredientIcon(value: IngredientIcon?): String? = value?.name

    @TypeConverter
    fun toIngredientIcon(value: String?): IngredientIcon? =
        value?.let {
            try {
                IngredientIcon.valueOf(it)
            } catch (e: Exception) {
                IngredientIcon.DEFAULT
            }
        }
}
