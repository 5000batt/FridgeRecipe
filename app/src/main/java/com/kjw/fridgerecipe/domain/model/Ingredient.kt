package com.kjw.fridgerecipe.domain.model

import java.time.LocalDate

enum class UnitType(val label: String) {
    COUNT("개"),
    GRAM("g"),
    MILLILITER("ml"),
    ETC("기타");

    companion object {
        fun fromString(label: String?): UnitType {
            return UnitType.entries.find { it.label == label } ?: UnitType.ETC
        }
    }
}

enum class StorageType(val label: String) {
    REFRIGERATED("냉장"),
    FROZEN("냉동"),
    ROOM_TEMPERATURE("실온");

    companion object {
        fun fromString(label: String?): StorageType {
            return StorageType.entries.find { it.label == label } ?: StorageType.REFRIGERATED
        }
    }
}

enum class CategoryType(val label: String) {
    ETC("기타"),
    VEGETABLE("채소"),
    FRUIT("과일"),
    MEAT("육류"),
    SEAFOOD("해산물"),
    DAIRY("유제품/계란"),
    GRAIN("곡물/면/빵"),
    BEVERAGE("음료"),
    SEASONING("소스/양념/조미료");

    companion object {
        fun fromString(label: String?): CategoryType {
            return CategoryType.entries.find { it.label == label } ?: CategoryType.ETC
        }
    }
}

data class Ingredient (
    val id: Long? = null,               // 객체 고유 식별자
    val name: String,                   // 이름
    val amount: Double,                 // 수량
    val unit: UnitType,                 // 단위
    val expirationDate: LocalDate,      // 소비기한
    val storageLocation: StorageType,   // 보관위치
    val emoticon: IngredientIcon,        // 이모티콘
    val category: CategoryType
)