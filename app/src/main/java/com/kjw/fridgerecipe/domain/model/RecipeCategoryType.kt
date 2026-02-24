package com.kjw.fridgerecipe.domain.model

import com.kjw.fridgerecipe.R

enum class RecipeCategoryType(
    val labelResId: Int,
    val id: String,
) {
    KOREAN(R.string.category_korean, "KOREAN"),
    JAPANESE(R.string.category_japanese, "JAPANESE"),
    CHINESE(R.string.category_chinese, "CHINESE"),
    WESTERN(R.string.category_western, "WESTERN"),
    ;

    companion object {
        fun fromId(id: String?): RecipeCategoryType? = entries.find { it.id == id }
    }
}
