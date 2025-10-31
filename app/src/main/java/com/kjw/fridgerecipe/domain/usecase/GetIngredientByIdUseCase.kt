package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import javax.inject.Inject

class GetIngredientByIdUseCase @Inject constructor(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(id: Long): Ingredient? {
        return repository.getIngredientById(id)
    }
}