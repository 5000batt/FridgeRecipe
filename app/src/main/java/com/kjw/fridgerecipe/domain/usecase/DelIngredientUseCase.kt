package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import com.kjw.fridgerecipe.domain.util.DataError
import com.kjw.fridgerecipe.domain.util.DataResult
import javax.inject.Inject

class DelIngredientUseCase
    @Inject
    constructor(
        private val ingredientRepository: IngredientRepository,
    ) {
        suspend operator fun invoke(ingredient: Ingredient): DataResult<Unit> {
            if (ingredient.id == null) {
                return DataResult.Error(DataError.DELETE_FAILED)
            }

            return ingredientRepository.deleteIngredient(ingredient)
        }
    }
