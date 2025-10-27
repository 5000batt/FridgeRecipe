package com.kjw.fridgerecipe.domain.model

import androidx.annotation.DrawableRes
import com.kjw.fridgerecipe.R

enum class IngredientIcon(@DrawableRes val iconResId: Int, val description: String) {
    CARROT(R.drawable.ic_launcher_foreground, "당근"),
    DEFAULT(R.drawable.default_image, "기본")
}