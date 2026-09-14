package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import com.kjw.fridgerecipe.domain.util.DataError
import com.kjw.fridgerecipe.domain.util.DataResult
import com.kjw.fridgerecipe.domain.util.validate
import javax.inject.Inject

class UpdateRecipeUseCase
    @Inject
    constructor(
        private val recipeRepository: RecipeRepository,
    ) {
        suspend operator fun invoke(recipe: Recipe): DataResult<Unit> {
            if (recipe.id == null) return DataResult.Error(DataError.RECIPE_NOT_FOUND)
            val validation = recipe.validate()
            if (validation is DataResult.Error) return validation
            return recipeRepository.updateRecipe(recipe)
        }
    }
