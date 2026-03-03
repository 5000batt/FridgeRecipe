package com.kjw.fridgerecipe.domain.model

enum class CookingToolType(
    val id: String,
) {
    AIR_FRYER("AIR_FRYER"),
    MICROWAVE("MICROWAVE"),
    POT("POT"),
    PAN("PAN"),
    ;

    companion object {
        fun fromId(id: String?): CookingToolType? = entries.find { it.id == id }
    }
}
