package com.kjw.fridgerecipe.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep

class RecipeTypeConverters {

    private val gson = Gson()

    @TypeConverter
    fun fromIngredientList(value: List<RecipeIngredient>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toIngredientList(value: String?): List<RecipeIngredient>? {
        val listType = object : TypeToken<List<RecipeIngredient>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromStepList(value: List<RecipeStep>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStepList(value: String?): List<RecipeStep>? {
        val listType = object : TypeToken<List<RecipeStep>>() {}.type
        return gson.fromJson(value, listType)
    }
}