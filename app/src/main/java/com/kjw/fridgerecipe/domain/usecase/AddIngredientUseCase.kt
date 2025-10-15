package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import javax.inject.Inject

class AddIngredientUseCase @Inject constructor(
    private val ingredientRepository: IngredientRepository
) {
    suspend operator fun invoke(ingredient: Ingredient): Boolean {

        // 유효성 검사 추후 추가 예정
        if (ingredient.name.isBlank()) {
            // 사용자 에러 메시지 전달
            return false
        }

        ingredientRepository.insertIngredient(ingredient)
        return true
    }
}