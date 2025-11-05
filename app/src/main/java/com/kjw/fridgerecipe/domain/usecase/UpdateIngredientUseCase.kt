package com.kjw.fridgerecipe.domain.usecase

import android.util.Log
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import java.lang.Exception
import javax.inject.Inject

class UpdateIngredientUseCase @Inject constructor(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(ingredient: Ingredient): Boolean {

        if (ingredient.id == null) {
            Log.e("UpdateIngredientUseCase", "ID가 null인 재료는 수정할 수 없습니다.")
            return false
        }

        if (ingredient.name.isBlank()) {
            return false
        }

         if (ingredient.amount <= 0) {
             return false
         }

        return try {
            repository.updateIngredient(ingredient)
            true
        } catch (e: Exception) {
            Log.e("UpdateIngredientUseCase", "DB 업데이트 실패: ${e.message}", e)
            false
        }
    }
}