package com.kjw.fridgerecipe.domain.model

enum class UnitType(
    val id: String,
    val symbol: String,
) {
    COUNT("COUNT", "개"),
    GRAM("GRAM", "g"),
    MILLILITER("MILLILITER", "ml"),
    ETC("ETC", ""),
    ;

    companion object {
        fun fromId(id: String?): UnitType = entries.find { it.id == id } ?: ETC
    }
}
