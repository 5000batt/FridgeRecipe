package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import com.kjw.fridgerecipe.domain.util.DataResult
import com.kjw.fridgerecipe.presentation.util.UiText
import javax.inject.Inject

class InsertRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(recipe: Recipe): DataResult<Long> {
        if (recipe.id != null) {
            return DataResult.Error(UiText.StringResource(R.string.error_recipe_id_exists))
        }

        return recipeRepository.insertRecipe(recipe)
    }
}
