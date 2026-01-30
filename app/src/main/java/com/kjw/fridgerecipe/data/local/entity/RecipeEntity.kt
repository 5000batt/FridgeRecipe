package com.kjw.fridgerecipe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val title: String,
    val servings: String,
    val time: String,
    val level: String,
    val ingredients: List<RecipeIngredient>,
    val steps: List<RecipeStep>,
    val imageUri: String? = null,

    // 검색 조건 및 필터 정보 통합
    val category: String? = null,
    val cookingTool: String? = null,
    val timeFilter: String? = null,
    val ingredientsQuery: String? = null,
    val useOnlySelected: Boolean = false
)
