package com.kjw.fridgerecipe.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

data class AiRecipeResponse(
    val recipe: RecipeDto
)

data class RecipeIngredientDto(val name: String?, val quantity: String?)
data class RecipeStepDto(val number: Int?, val description: String?)
data class RecipeInfoDto(val servings: String?, val time: String?, val level: String?)
data class RecipeDto(
    val title: String?,
    val info: RecipeInfoDto?,
    val ingredients: List<RecipeIngredientDto>?,
    val steps: List<RecipeStepDto>?
)

data class GeminiRequest(
    val contents: List<Content>
) {
    data class Content(val parts: List<Part>)
    data class Part(val text: String)
}

data class GeminiResponse(
    val candidates: List<Candidate>?
) {
    data class Candidate(val content: Content?)
    data class Content(val parts: List<Part>?)
    data class Part(val text: String?)
}

interface ApiService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun getGeminiRecipe(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
