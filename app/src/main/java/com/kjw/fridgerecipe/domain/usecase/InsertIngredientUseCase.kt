package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import com.kjw.fridgerecipe.domain.util.DataResult
import com.kjw.fridgerecipe.domain.util.validate
import javax.inject.Inject

class InsertIngredientUseCase
    @Inject
    constructor(
        private val ingredientRepository: IngredientRepository,
    ) {
        suspend operator fun invoke(ingredient: Ingredient): DataResult<Unit> {
            val validation = ingredient.validate()
            if (validation is DataResult.Error) return validation
            return ingredientRepository.insertIngredient(ingredient)
        }
    }
