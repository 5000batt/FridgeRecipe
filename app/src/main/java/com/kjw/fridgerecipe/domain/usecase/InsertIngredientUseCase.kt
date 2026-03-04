package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import com.kjw.fridgerecipe.domain.util.DataError
import com.kjw.fridgerecipe.domain.util.DataResult
import javax.inject.Inject

class InsertIngredientUseCase
    @Inject
    constructor(
        private val ingredientRepository: IngredientRepository,
    ) {
        suspend operator fun invoke(ingredient: Ingredient): DataResult<Unit> {
            // 비즈니스 유효성 검사
            if (ingredient.name.isBlank()) {
                return DataResult.Error(DataError.EMPTY_NAME)
            }

            if (ingredient.amount <= 0) {
                return DataResult.Error(DataError.INVALID_AMOUNT)
            }

            return ingredientRepository.insertIngredient(ingredient)
        }
    }
