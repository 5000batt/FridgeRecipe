package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import javax.inject.Inject

class DelIngredientUseCase @Inject constructor(
    private val ingredientRepository: IngredientRepository
) {
    suspend operator fun invoke(ingredient: Ingredient): Boolean {

        if (ingredient.id == null) {
            // 사용자 에러 메시지 전달
            return false
        }

        ingredientRepository.deleteIngredient(ingredient)
        return true
    }
}