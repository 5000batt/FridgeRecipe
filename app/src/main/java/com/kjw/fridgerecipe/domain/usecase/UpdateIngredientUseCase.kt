package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import com.kjw.fridgerecipe.domain.util.DataError
import com.kjw.fridgerecipe.domain.util.DataResult
import com.kjw.fridgerecipe.domain.util.validate
import javax.inject.Inject

class UpdateIngredientUseCase
    @Inject
    constructor(
        private val ingredientRepository: IngredientRepository,
    ) {
        suspend operator fun invoke(ingredient: Ingredient): DataResult<Unit> {
            if (ingredient.id == null) return DataResult.Error(DataError.INGREDIENT_NOT_FOUND)
            val validation = ingredient.validate()
            if (validation is DataResult.Error) return validation
            return ingredientRepository.updateIngredient(ingredient)
        }
    }
