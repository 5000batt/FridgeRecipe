package com.kjw.fridgerecipe.presentation.validator

import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.presentation.ui.model.ListErrorType
import com.kjw.fridgerecipe.presentation.ui.model.RecipeEditUiState
import com.kjw.fridgerecipe.presentation.ui.model.RecipeValidationField
import com.kjw.fridgerecipe.presentation.util.UiText
import javax.inject.Inject

class RecipeValidator
    @Inject
    constructor() {
        sealed class ValidationResult {
            data object Success : ValidationResult()

            data class Failure(
                val field: RecipeValidationField,
                val errorMessage: UiText,
                val listErrorType: ListErrorType = ListErrorType.NONE,
            ) : ValidationResult()
        }

        fun validate(state: RecipeEditUiState): ValidationResult =
            when {
                state.title.isBlank() ->
                    ValidationResult.Failure(RecipeValidationField.TITLE, UiText.StringResource(R.string.error_recipe_title_empty))

                state.servingsState.isBlank() ->
                    ValidationResult.Failure(RecipeValidationField.SERVINGS, UiText.StringResource(R.string.error_recipe_servings_empty))

                state.timeState.isBlank() ->
                    ValidationResult.Failure(RecipeValidationField.TIME, UiText.StringResource(R.string.error_recipe_time_empty))

                state.ingredientsState.isEmpty() ->
                    ValidationResult.Failure(
                        RecipeValidationField.INGREDIENTS,
                        UiText.StringResource(R.string.error_recipe_ingredients_empty),
                        ListErrorType.IS_EMPTY,
                    )

                state.ingredientsState.any { it.name.isBlank() || it.quantity.isBlank() } ->
                    ValidationResult.Failure(
                        RecipeValidationField.INGREDIENTS,
                        UiText.StringResource(R.string.error_recipe_ingredients_blank),
                        ListErrorType.HAS_BLANK_ITEMS,
                    )

                state.stepsState.isEmpty() ->
                    ValidationResult.Failure(
                        RecipeValidationField.STEPS,
                        UiText.StringResource(R.string.error_recipe_steps_empty),
                        ListErrorType.IS_EMPTY,
                    )

                state.stepsState.any { it.description.isBlank() } ->
                    ValidationResult.Failure(
                        RecipeValidationField.STEPS,
                        UiText.StringResource(R.string.error_recipe_steps_blank),
                        ListErrorType.HAS_BLANK_ITEMS,
                    )

                else -> ValidationResult.Success
            }
    }
