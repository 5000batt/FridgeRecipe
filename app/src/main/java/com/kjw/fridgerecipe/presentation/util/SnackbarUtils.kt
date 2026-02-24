package com.kjw.fridgerecipe.presentation.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals

enum class SnackbarType {
    SUCCESS,
    ERROR,
    INFO,
}

class CustomSnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val withDismissAction: Boolean = false,
    val type: SnackbarType,
) : SnackbarVisuals
