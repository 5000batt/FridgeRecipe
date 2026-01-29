package com.kjw.fridgerecipe.domain.model

import java.time.LocalDate

data class Ingredient (
    val id: Long? = null,               // 객체 고유 식별자
    val name: String,                   // 이름
    val amount: Double,                 // 수량
    val unit: UnitType,                 // 단위
    val expirationDate: LocalDate,      // 소비기한
    val storageLocation: StorageType,   // 보관위치
    val emoticon: IngredientIcon,        // 이모티콘
    val category: IngredientCategoryType
)