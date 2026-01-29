package com.kjw.fridgerecipe.data.datasource

import com.kjw.fridgerecipe.data.remote.RecipeDto
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType

interface RecipeRemoteDataSource {
    suspend fun getAiRecipe(
        ingredients: List<Ingredient>,
        timeFilter: String?,
        levelFilter: LevelType?,
        categoryFilter: String?,
        utensilFilter: String?,
        useOnlySelected: Boolean,
        excludedIngredients: List<String>
    ): RecipeDto
}