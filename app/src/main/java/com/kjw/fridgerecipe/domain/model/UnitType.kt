package com.kjw.fridgerecipe.domain.model

import com.kjw.fridgerecipe.R

enum class UnitType(
    val labelResId: Int,
    val id: String,
    val symbol: String,
) {
    COUNT(R.string.unit_count, "COUNT", "개"),
    GRAM(R.string.unit_gram, "GRAM", "g"),
    MILLILITER(R.string.unit_milliliter, "MILLILITER", "ml"),
    ETC(R.string.unit_etc, "ETC", ""),
    ;

    companion object {
        fun fromId(id: String?): UnitType = entries.find { it.id == id } ?: ETC
    }
}
