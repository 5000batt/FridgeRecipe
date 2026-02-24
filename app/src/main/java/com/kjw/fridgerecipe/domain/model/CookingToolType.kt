package com.kjw.fridgerecipe.domain.model

import com.kjw.fridgerecipe.R

enum class CookingToolType(
    val labelResId: Int,
    val id: String,
) {
    AIR_FRYER(R.string.cooking_tool_airfryer, "AIR_FRYER"),
    MICROWAVE(R.string.cooking_tool_microwave, "MICROWAVE"),
    POT(R.string.cooking_tool_pot, "POT"),
    PAN(R.string.cooking_tool_pan, "PAN"),
    ;

    companion object {
        fun fromId(id: String?): CookingToolType? = entries.find { it.id == id }
    }
}
