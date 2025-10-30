package com.kjw.fridgerecipe.domain.usecase

import android.util.Log
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import javax.inject.Inject

class AddIngredientUseCase @Inject constructor(
    private val ingredientRepository: IngredientRepository
) {
    suspend operator fun invoke(ingredient: Ingredient): Boolean {

        if (ingredient.name.isBlank()) {
            return false
        }

        if (ingredient.amount <= 0) {
            return false
        }

        return try {
            ingredientRepository.insertIngredient(ingredient)
            true
        } catch (e: Exception) {
            Log.e("AddIngredientUseCase", "DB 저장 실패: ${e.message}", e)
            false
        }
    }
}