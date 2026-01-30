package com.kjw.fridgerecipe.data.local.converter

import androidx.room.TypeConverter
import com.kjw.fridgerecipe.data.util.AppJson
import com.kjw.fridgerecipe.domain.model.CookingToolType
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep
import kotlinx.serialization.encodeToString

/**
 * Recipe 관련 복합 타입을 DB에 저장하기 위한 컨버터.
 * [AppJson] 싱글톤을 사용하여 직렬화 성능을 최적화합니다.
 */
class RecipeTypeConverters {

    private val json = AppJson.default

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

    // --- Enum Converters ---

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
