package com.kjw.fridgerecipe.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class LevelType(
    val id: String,
) {
    BEGINNER("BEGINNER"),
    INTERMEDIATE("INTERMEDIATE"),
    ADVANCED("ADVANCED"),
    ETC("ETC"),
    ;

    companion object {
        fun fromId(id: String?): LevelType = entries.find { it.id == id } ?: ETC
    }
}
