package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import com.kjw.fridgerecipe.domain.util.DataError
import com.kjw.fridgerecipe.domain.util.DataResult
import javax.inject.Inject

class UpdateIngredientUseCase
    @Inject
    constructor(
        private val repository: IngredientRepository,
    ) {
        suspend operator fun invoke(ingredient: Ingredient): DataResult<Unit> {
            if (ingredient.id == null) {
                return DataResult.Error(DataError.UNKNOWN)
            }

            if (ingredient.name.isBlank()) {
                return DataResult.Error(DataError.EMPTY_NAME)
            }

            if (ingredient.amount <= 0) {
                return DataResult.Error(DataError.INVALID_AMOUNT)
            }

            return repository.updateIngredient(ingredient)
        }
    }
