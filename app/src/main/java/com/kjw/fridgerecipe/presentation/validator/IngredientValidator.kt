package com.kjw.fridgerecipe.presentation.validator

import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.presentation.ui.model.IngredientEditUiState
import com.kjw.fridgerecipe.presentation.ui.model.IngredientValidationField
import com.kjw.fridgerecipe.presentation.util.UiText
import javax.inject.Inject

class IngredientValidator @Inject constructor() {

    sealed class ValidationResult {
        data object Success : ValidationResult()
        data class Failure(
            val field: IngredientValidationField,
            val errorMessage: UiText
        ) : ValidationResult()
    }

    fun validate(state: IngredientEditUiState): ValidationResult {
        return when {
            state.name.isBlank() ->
                ValidationResult.Failure(
                    IngredientValidationField.NAME,
                    UiText.StringResource(R.string.error_validation_name_empty)
                )

            state.amount.isBlank() ->
                ValidationResult.Failure(
                    IngredientValidationField.AMOUNT,
                    UiText.StringResource(R.string.error_validation_amount_empty)
                )

            else -> ValidationResult.Success
        }
    }
}
