package com.kjw.fridgerecipe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val title: String,
    val servings: String,
    val time: String,
    val level: String,
    val ingredients: String,
    val steps: String,
    // 검색 필터
    val ingredientsQuery: String?,
    val timeFilter: String?,
    val levelFilter: String?,
    val categoryFilter: String?,
    val utensilFilter: String?,
    val useOnlySelected: Boolean
)
