package com.kjw.fridgerecipe.presentation.ui.common

sealed class OperationResult {
    data class Success(val message: String) : OperationResult()
    data class Failure(val message: String) : OperationResult()
}