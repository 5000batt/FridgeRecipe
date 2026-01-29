package com.kjw.fridgerecipe.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep

data class RecipeSearchMetadataEntity(
    val ingredientsQuery: String?,
    val timeFilter: String?,
    val levelFilter: String?,
    val categoryFilter: String?,
    val cookingToolFilter: String?,
    val useOnlySelected: Boolean
)

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
    @Embedded(prefix = "search_")
    val searchMetadata: RecipeSearchMetadataEntity?
)
