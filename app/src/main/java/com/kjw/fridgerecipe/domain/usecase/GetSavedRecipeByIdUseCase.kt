package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import com.kjw.fridgerecipe.domain.util.DataResult
import javax.inject.Inject

class GetSavedRecipeByIdUseCase
    @Inject
    constructor(
        private val repository: RecipeRepository,
    ) {
        suspend operator fun invoke(id: Long): DataResult<Recipe> = repository.getSavedRecipeById(id)
    }
