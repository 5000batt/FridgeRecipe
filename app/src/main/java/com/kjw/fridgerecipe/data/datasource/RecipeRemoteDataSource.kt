package com.kjw.fridgerecipe.data.datasource

import com.kjw.fridgerecipe.data.remote.RecipeDto
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.model.CookingToolType

interface RecipeRemoteDataSource {
    suspend fun getAiRecipe(
        ingredients: List<Ingredient>,
        timeFilter: String?,
        level: LevelType?,
        categoryFilter: RecipeCategoryType?,
        cookingToolFilter: CookingToolType?,
        useOnlySelected: Boolean,
        excludedIngredients: List<String>
    ): RecipeDto
}
