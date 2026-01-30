package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import com.kjw.fridgerecipe.domain.util.DataResult
import com.kjw.fridgerecipe.presentation.util.UiText
import javax.inject.Inject

class DelIngredientUseCase @Inject constructor(
    private val ingredientRepository: IngredientRepository
) {
    suspend operator fun invoke(ingredient: Ingredient): DataResult<Unit> {
        if (ingredient.id == null) {
            return DataResult.Error(UiText.StringResource(R.string.error_delete_failed))
        }

        return ingredientRepository.deleteIngredient(ingredient)
    }
}
