package com.kjw.fridgerecipe.presentation.util

import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.IngredientIcon

fun getIconResId(icon: IngredientIcon): Int {
    return when (icon) {
        IngredientIcon.CARROT -> R.drawable.ic_launcher_foreground
        else -> R.drawable.default_image
    }
}