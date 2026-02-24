package com.kjw.fridgerecipe.domain.model

import com.kjw.fridgerecipe.R

enum class IngredientCategoryType(
    val labelResId: Int,
    val id: String,
) {
    ETC(R.string.ingredient_category_etc, "ETC"),
    VEGETABLE(R.string.ingredient_category_vegetable, "VEGETABLE"),
    FRUIT(R.string.ingredient_category_fruit, "FRUIT"),
    MEAT(R.string.ingredient_category_meat, "MEAT"),
    SEAFOOD(R.string.ingredient_category_seafood, "SEAFOOD"),
    DAIRY(R.string.ingredient_category_dairy, "DAIRY"),
    GRAIN(R.string.ingredient_category_grain, "GRAIN"),
    BEVERAGE(R.string.ingredient_category_beverage, "BEVERAGE"),
    SEASONING(R.string.ingredient_category_seasoning, "SEASONING"),
    ;

    companion object {
        fun fromId(id: String?): IngredientCategoryType = entries.find { it.id == id } ?: ETC
    }
}
