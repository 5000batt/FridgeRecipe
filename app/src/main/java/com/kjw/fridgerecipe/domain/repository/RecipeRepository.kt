package com.kjw.fridgerecipe.domain.repository

import com.kjw.fridgerecipe.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    suspend fun getAiRecipes(prompt: String, ingredientsQuery: String): Recipe?

    fun getAllSavedRecipes(): Flow<List<Recipe>>

    suspend fun getSavedRecipeById(id: Long): Recipe?
    suspend fun findSavedRecipeByQuery(query: String): List<Recipe>
}