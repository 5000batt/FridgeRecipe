package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import com.kjw.fridgerecipe.domain.util.DataError
import com.kjw.fridgerecipe.domain.util.DataResult
import javax.inject.Inject

class UpdateRecipeUseCase
    @Inject
    constructor(
        private val recipeRepository: RecipeRepository,
    ) {
        suspend operator fun invoke(recipe: Recipe): DataResult<Unit> {
            if (recipe.id == null) {
                return DataResult.Error(DataError.RECIPE_NOT_FOUND)
            }

            if (recipe.title.isBlank()) {
                return DataResult.Error(DataError.RECIPE_EMPTY_TITLE)
            }

            if (recipe.servings <= 0) {
                return DataResult.Error(DataError.RECIPE_INVALID_SERVINGS)
            }

            if (recipe.time <= 0) {
                return DataResult.Error(DataError.RECIPE_INVALID_TIME)
            }

            if (recipe.ingredients.isEmpty()) {
                return DataResult.Error(DataError.RECIPE_EMPTY_INGREDIENTS)
            }

            if (recipe.ingredients.any { it.name.isBlank() || it.quantity.isBlank() }) {
                return DataResult.Error(DataError.RECIPE_INVALID_INGREDIENT_ITEM)
            }

            if (recipe.steps.isEmpty()) {
                return DataResult.Error(DataError.RECIPE_EMPTY_STEPS)
            }

            if (recipe.steps.any { it.description.isBlank() }) {
                return DataResult.Error(DataError.RECIPE_INVALID_STEP_ITEM)
            }

            return recipeRepository.updateRecipe(recipe)
        }
    }
