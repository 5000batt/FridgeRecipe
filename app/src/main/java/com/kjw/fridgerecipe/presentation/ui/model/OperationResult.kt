package com.kjw.fridgerecipe.presentation.ui.model

import com.kjw.fridgerecipe.presentation.util.UiText

sealed class OperationResult {
    data class Success(
        val message: UiText,
    ) : OperationResult()

    data class Failure(
        val message: UiText,
    ) : OperationResult()
}
