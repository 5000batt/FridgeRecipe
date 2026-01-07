package com.kjw.fridgerecipe.data.local.converter

import androidx.room.TypeConverter
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RecipeTypeConverters {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    @TypeConverter
    fun fromIngredientList(value: List<RecipeIngredient>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toIngredientList(value: String?): List<RecipeIngredient>? {
        return value?.let {
            try {
                json.decodeFromString<List<RecipeIngredient>>(it)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    @TypeConverter
    fun fromStepList(value: List<RecipeStep>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toStepList(value: String?): List<RecipeStep>? {
        return value?.let {
            try {
                json.decodeFromString<List<RecipeStep>>(it)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}