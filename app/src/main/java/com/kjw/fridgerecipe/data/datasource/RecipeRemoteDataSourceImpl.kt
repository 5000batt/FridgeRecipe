package com.kjw.fridgerecipe.data.datasource

import android.util.Log
import com.google.firebase.ai.type.InvalidAPIKeyException
import com.google.firebase.ai.type.PromptBlockedException
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.ResponseStoppedException
import com.google.firebase.ai.type.ServerException
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.kjw.fridgerecipe.data.remote.AiRecipeResponse
import com.kjw.fridgerecipe.data.remote.GeminiModelProvider
import com.kjw.fridgerecipe.data.remote.RecipeDto
import com.kjw.fridgerecipe.data.remote.RecipePromptGenerator
import com.kjw.fridgerecipe.domain.model.GeminiException
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import kotlinx.serialization.json.Json
import javax.inject.Inject

class RecipeRemoteDataSourceImpl @Inject constructor(
    private val promptGenerator: RecipePromptGenerator,
    private val modelProvider: GeminiModelProvider,
    private val remoteConfig: FirebaseRemoteConfig
) : RecipeRemoteDataSource {

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    override suspend fun getAiRecipe(
        ingredients: List<Ingredient>,
        timeFilter: String?,
        levelFilter: LevelType?,
        categoryFilter: String?,
        utensilFilter: String?,
        useOnlySelected: Boolean,
        excludedIngredients: List<String>
    ): RecipeDto {
        val template = remoteConfig.getString("recipe_prompt_template")

        // 프롬프트 생성
        val prompt = promptGenerator.createRecipePrompt(
            template = template,
            ingredients = ingredients,
            timeFilter = timeFilter,
            levelFilter = levelFilter,
            categoryFilter = categoryFilter,
            utensilFilter = utensilFilter,
            useOnlySelected = useOnlySelected,
            excludedIngredients = excludedIngredients
        )

        Log.d("RemoteDataSource", "프롬프트 내용 : $prompt")

        try {
            // AI 호출
            val generativeModel = modelProvider.getModel()
            val response = generativeModel.generateContent(prompt)
            val aiResponseText = response.text

            if (aiResponseText == null) {
                Log.e("RemoteDataSource", "AI 응답이 비어있습니다.")
                throw GeminiException.ParsingError()
            }

            Log.d("RemoteDataSource", "AI 원본 응답: $aiResponseText")

            // JSON 파싱
            val jsonString = extractJsonString(aiResponseText)
            val recipeResponse = jsonParser.decodeFromString<AiRecipeResponse>(jsonString)

            return recipeResponse.recipe

        } catch (e: Exception) {
            Log.e("RemoteDataSource", "AI 레시피 호출 실패", e)
            throw mapToGeminiException(e)
        }
    }

    // Json 문자열만 추출
    private fun extractJsonString(text: String): String {
        val codeBlockRegex = Regex("```(?:json)?\\s*([\\s\\S]*?)\\s*```")
        val matchResult = codeBlockRegex.find(text)

        val rawJson = if (matchResult != null) {
            matchResult.groupValues[1]
        } else {
            text
        }

        val startIndex = rawJson.indexOf('{')
        val endIndex = rawJson.lastIndexOf('}')

        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            throw GeminiException.ParsingError()
        }

        return rawJson.substring(startIndex, endIndex + 1)
    }

    private fun mapToGeminiException(e: Exception): GeminiException {
        var cause = e.cause

        while (cause != null) {
            if (cause is java.io.IOException ||
                cause is java.net.UnknownHostException ||
                cause is java.net.SocketTimeoutException ||
                cause is java.net.ConnectException
            ) {
                return GeminiException.NetworkError()
            }
            cause = cause.cause
        }

        return when (e) {
            // 1. API 키 오류 (403)
            is InvalidAPIKeyException -> GeminiException.ApiKeyError()

            // 2. 쿼터 초과 (429) - 무료 사용량 초과 등
            is QuotaExceededException -> GeminiException.QuotaExceeded()

            // 3. 안전 필터에 걸림 (성적, 혐오 표현 등)
            is PromptBlockedException,
            is ResponseStoppedException -> GeminiException.ResponseBlocked()

            // 4. 서버 오류 (500, 503)
            is ServerException -> GeminiException.ServerOverloaded()

            // 5. 파싱 오류 (JSON 형식이 아님)
            is kotlinx.serialization.SerializationException,
            is IllegalArgumentException -> GeminiException.ParsingError()

            // 6. 인터넷 연결 문제 (IOException)
            // Firebase SDK도 내부적으로 네트워크 통신 시 IOException을 던집니다.
            is java.io.IOException,
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException -> GeminiException.NetworkError()

            // 7. 그 외 알 수 없는 오류
            else -> {
                // 혹시 SDK가 구체적인 Exception Class 대신 ServerException 안에 메시지로 에러를 줄 경우를 대비
                val msg = e.message ?: ""

                when {
                    msg.contains("429") || msg.contains("Quota") -> GeminiException.QuotaExceeded()
                    msg.contains("403") || msg.contains("API key") -> GeminiException.ApiKeyError()
                    msg.contains("503") || msg.contains("Overloaded") -> GeminiException.ServerOverloaded()
                    msg.contains("Failed to connect") || msg.contains("timeout") -> GeminiException.NetworkError()
                    msg.contains("400") -> GeminiException.Unknown(400)
                    else -> GeminiException.Unknown(-1)
                }
            }
        }
    }
}