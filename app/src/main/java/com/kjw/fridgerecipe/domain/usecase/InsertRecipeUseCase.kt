package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import com.kjw.fridgerecipe.domain.util.DataError
import com.kjw.fridgerecipe.domain.util.DataResult
import com.kjw.fridgerecipe.domain.util.validate
import javax.inject.Inject

class InsertRecipeUseCase
    @Inject
    constructor(
        private val recipeRepository: RecipeRepository,
    ) {
        suspend operator fun invoke(recipe: Recipe): DataResult<Long> {
            if (recipe.id != null) return DataResult.Error(DataError.RECIPE_ALREADY_EXISTS)
            val validation = recipe.validate()
            if (validation is DataResult.Error) return validation
            return recipeRepository.insertRecipe(recipe)
        }
    }
