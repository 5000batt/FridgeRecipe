package com.kjw.fridgerecipe.domain.usecase

import android.util.Log
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import javax.inject.Inject

class InsertRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(recipe: Recipe): Boolean {
        if (recipe.id != null) {
            Log.e("AddRecipeUseCase", "ID가 있는 레시피는 추가할 수 없습니다. (Update 사용)")
            return false
        }

        return try {
            recipeRepository.insertRecipe(recipe)
            true
        } catch (e: Exception) {
            Log.e("AddRecipeUseCase", "DB 삽입 실패: ${e.message}", e)
            false
        }
    }
}