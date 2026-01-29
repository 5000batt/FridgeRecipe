package com.kjw.fridgerecipe.domain.model

import com.kjw.fridgerecipe.R
import kotlinx.serialization.Serializable

@Serializable
enum class LevelType(val labelResId: Int, val id: String) {
    BEGINNER(R.string.level_beginner, "BEGINNER"),
    INTERMEDIATE(R.string.level_intermediate, "INTERMEDIATE"),
    ADVANCED(R.string.level_advanced, "ADVANCED"),
    ETC(R.string.level_etc, "ETC");

    companion object {
        fun fromId(id: String?): LevelType {
            return entries.find { it.id == id } ?: ETC
        }
    }
}
