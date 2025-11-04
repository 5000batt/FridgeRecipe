package com.kjw.fridgerecipe.data.repository

import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TestRecipeRepository @Inject constructor() : RecipeRepository {
    override suspend fun getAiRecipes(prompt: String): Recipe {

        delay(1500)

        return Recipe(
            title = "계란말이",
            servings = "1-2인분",
            time = "15분",
            level = "초급",
            ingredients = listOf(
                RecipeIngredient(name = "계란 (보유)", quantity = "3개"),
                RecipeIngredient(name = "양파 (보유)", quantity = "1/4개"),
                RecipeIngredient(name = "당근 (부족)", quantity = "조금"),
                RecipeIngredient(name = "소금", quantity = "약간")
            ),
            steps = listOf(
                RecipeStep(number = 1, description = "계란을 그릇에 잘 풀어줍니다."),
                RecipeStep(number = 2, description = "양파와 당근을 잘게 다집니다."),
                RecipeStep(number = 3, description = "계란물에 다진 채소와 소금을 넣고 섞습니다."),
                RecipeStep(number = 4, description = "기름을 두른 팬에 약불로 천천히 말아줍니다.")
            )
        )
    }

    override fun getAllSavedRecipes(): Flow<List<Recipe>> {
        TODO("Not yet implemented")
    }
}