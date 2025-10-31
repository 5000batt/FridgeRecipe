package com.kjw.fridgerecipe.domain.usecase

import android.util.Log
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import javax.inject.Inject

class DelIngredientUseCase @Inject constructor(
    private val ingredientRepository: IngredientRepository
) {
    suspend operator fun invoke(ingredient: Ingredient): Boolean {

        if (ingredient.id == null) {
            Log.e("DelIngredientUseCase", "ID가 null인 재료는 삭제할 수 없습니다.")
            return false
        }

        return try {
            ingredientRepository.deleteIngredient(ingredient)
            true
        } catch (e: Exception) {
            Log.e("DelIngredientUseCase", "DB 삭제 실패: ${e.message}", e)
            false
        }
    }
}