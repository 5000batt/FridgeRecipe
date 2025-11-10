package com.kjw.fridgerecipe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val name: String,
    val amount: Double,
    val unitName: String,
    val expirationDate: LocalDate,
    val storageLocationName: String,
    val categoryName: String,
    val emoticonName: String
)