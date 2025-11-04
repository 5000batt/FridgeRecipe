package com.kjw.fridgerecipe.domain.repository

import com.kjw.fridgerecipe.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    suspend fun getAiRecipes(prompt: String): Recipe?

    fun getAllSavedRecipes(): Flow<List<Recipe>>
}