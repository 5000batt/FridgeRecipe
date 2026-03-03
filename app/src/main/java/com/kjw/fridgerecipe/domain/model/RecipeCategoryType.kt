package com.kjw.fridgerecipe.domain.model

enum class RecipeCategoryType(
    val id: String,
) {
    KOREAN("KOREAN"),
    JAPANESE("JAPANESE"),
    CHINESE("CHINESE"),
    WESTERN("WESTERN"),
    ;

    companion object {
        fun fromId(id: String?): RecipeCategoryType? = entries.find { it.id == id }
    }
}
