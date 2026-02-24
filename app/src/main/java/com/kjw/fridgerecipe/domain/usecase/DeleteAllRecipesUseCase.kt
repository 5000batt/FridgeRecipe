package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import javax.inject.Inject

class DeleteAllRecipesUseCase
    @Inject
    constructor(
        private val repository: RecipeRepository,
    ) {
        suspend operator fun invoke() = repository.deleteAllRecipes()
    }
