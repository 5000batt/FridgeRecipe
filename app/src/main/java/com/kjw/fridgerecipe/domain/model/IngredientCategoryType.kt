package com.kjw.fridgerecipe.domain.model

enum class IngredientCategoryType(
    val id: String,
) {
    ETC("ETC"),
    VEGETABLE("VEGETABLE"),
    FRUIT("FRUIT"),
    MEAT("MEAT"),
    SEAFOOD("SEAFOOD"),
    DAIRY("DAIRY"),
    GRAIN("GRAIN"),
    BEVERAGE("BEVERAGE"),
    SEASONING("SEASONING"),
    ;

    companion object {
        fun fromId(id: String?): IngredientCategoryType = entries.find { it.id == id } ?: ETC
    }
}
