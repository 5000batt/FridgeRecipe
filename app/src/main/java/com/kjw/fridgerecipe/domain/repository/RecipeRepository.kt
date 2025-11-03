package com.kjw.fridgerecipe.domain.repository

import com.kjw.fridgerecipe.domain.model.Recipe

interface RecipeRepository {
    suspend fun getAiRecipes(prompt: String): Recipe?
}