package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetIngredientsUseCase @Inject constructor(
    private val ingredientRepository: IngredientRepository
) {

    operator fun invoke(): Flow<List<Ingredient>> {
        return ingredientRepository.getAllIngredients()
    }
}