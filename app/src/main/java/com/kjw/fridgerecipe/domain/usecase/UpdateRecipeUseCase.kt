package com.kjw.fridgerecipe.domain.usecase

import android.util.Log
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import javax.inject.Inject

class UpdateRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(recipe: Recipe): Boolean {

        return try {
            recipeRepository.updateRecipe(recipe)
            true
        } catch (e: Exception) {
            Log.e("UpdateRecipeUseCase", "DB 업데이트 실패: ${e.message}", e)
            false
        }
    }
}