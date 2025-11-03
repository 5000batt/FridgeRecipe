package com.kjw.fridgerecipe.data.repository

import android.util.Log
import com.google.gson.Gson
import com.kjw.fridgerecipe.BuildConfig
import com.kjw.fridgerecipe.data.remote.AiRecipeResponse
import com.kjw.fridgerecipe.data.remote.ApiService
import com.kjw.fridgerecipe.data.remote.GeminiRequest
import com.kjw.fridgerecipe.data.repository.mapper.toDomainModel
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson
) : RecipeRepository {

    override suspend fun getAiRecipes(prompt: String): Recipe? {

        val geminiRequest = GeminiRequest(
            contents = listOf(GeminiRequest.Content(parts = listOf(GeminiRequest.Part(text = prompt))))
        )

        val apiKey = BuildConfig.API_KEY

        return try {
            val geminiResponse = apiService.getGeminiRecipe(apiKey = apiKey, request = geminiRequest)
            var aiResponseText = geminiResponse.candidates?.firstOrNull()
                                    ?.content?.parts?.firstOrNull()?.text

            if (aiResponseText == null) {
                Log.e("RecipeRepo", "AI 응답이 비어있습니다.")
                return null
            }

            Log.e("RecipeRepo", "AI 원본 응답: $aiResponseText")

            aiResponseText = aiResponseText
                .trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val recipeResponse = gson.fromJson(aiResponseText, AiRecipeResponse::class.java)
            recipeResponse.recipe.toDomainModel()

        } catch (e: Exception) {
            Log.e("RecipeRepo", "AI 레시피 호출 실패", e)
            null
        }
    }
}