package com.kjw.fridgerecipe.data.local.converter

import androidx.room.TypeConverter
import com.kjw.fridgerecipe.domain.model.CookingToolType
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RecipeTypeConverters {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }

    // --- List Converters ---

    @TypeConverter
    fun fromIngredientList(value: List<RecipeIngredient>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toIngredientList(value: String?): List<RecipeIngredient> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<RecipeIngredient>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStepList(value: List<RecipeStep>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toStepList(value: String?): List<RecipeStep> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<RecipeStep>>(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Enum Converters (Type Safety 강화) ---

    @TypeConverter
    fun fromLevelType(value: LevelType?): String? = value?.id

    @TypeConverter
    fun toLevelType(value: String?): LevelType? = LevelType.fromId(value)

    @TypeConverter
    fun fromCategoryType(value: RecipeCategoryType?): String? = value?.id

    @TypeConverter
    fun toCategoryType(value: String?): RecipeCategoryType? = RecipeCategoryType.fromId(value)

    @TypeConverter
    fun fromCookingToolType(value: CookingToolType?): String? = value?.id

    @TypeConverter
    fun toCookingToolType(value: String?): CookingToolType? = CookingToolType.fromId(value)
}
