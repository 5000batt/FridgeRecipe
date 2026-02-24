package com.kjw.fridgerecipe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kjw.fridgerecipe.domain.model.IngredientCategoryType
import com.kjw.fridgerecipe.domain.model.IngredientIcon
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.domain.model.UnitType
import java.time.LocalDate

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val name: String,
    val amount: Double,
    val unit: UnitType,
    val expirationDate: LocalDate,
    val storageLocation: StorageType,
    val category: IngredientCategoryType,
    val emoticon: IngredientIcon,
)
