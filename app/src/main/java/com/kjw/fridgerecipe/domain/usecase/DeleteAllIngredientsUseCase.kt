package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import javax.inject.Inject

class DeleteAllIngredientsUseCase @Inject constructor(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke() = repository.deleteAllIngredients()
}