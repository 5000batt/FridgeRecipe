package com.kjw.fridgerecipe.data.repository

import android.util.Log
import com.google.firebase.ai.type.InvalidAPIKeyException
import com.google.firebase.ai.type.PromptBlockedException
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.ResponseStoppedException
import com.google.firebase.ai.type.ServerException
import com.kjw.fridgerecipe.data.local.dao.RecipeDao
import com.kjw.fridgerecipe.data.remote.AiRecipeResponse
import com.kjw.fridgerecipe.data.remote.GeminiModelProvider
import com.kjw.fridgerecipe.data.remote.RecipePromptGenerator
import com.kjw.fridgerecipe.data.repository.mapper.toDomainModel
import com.kjw.fridgerecipe.data.repository.mapper.toEntity
import com.kjw.fridgerecipe.domain.model.GeminiException
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeSearchMetadata
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import com.kjw.fridgerecipe.presentation.util.RecipeConstants.FILTER_ANY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val recipeDao: RecipeDao,
    private val promptGenerator: RecipePromptGenerator,
    private val modelProvider: GeminiModelProvider
) : RecipeRepository {

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private fun sanitizeFilter(value: String?): String? {
        return if (value == FILTER_ANY || value.isNullOrBlank()) null else value
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

    override suspend fun getAiRecipes(
        ingredients: List<Ingredient>,
        ingredientsQuery: String,
        timeFilter: String?,
        levelFilter: LevelType?,
        categoryFilter: String?,
        utensilFilter: String?,
        useOnlySelected: Boolean,
        excludedIngredients: List<String>
    ): Recipe? {

        val safeTime = sanitizeFilter(timeFilter)
        val safeCategory = sanitizeFilter(categoryFilter)
        val safeUtensil = sanitizeFilter(utensilFilter)

        val prompt = promptGenerator.createRecipePrompt(
            ingredients = ingredients,
            timeFilter = safeTime,
            levelFilter = levelFilter,
            categoryFilter = safeCategory,
            utensilFilter = safeUtensil,
            useOnlySelected = useOnlySelected,
            excludedIngredients = excludedIngredients
        )

        Log.d("RecipeRepo", "프롬프트 내용 : $prompt")

        try {
            val generativeModel = modelProvider.getModel()

            val response = generativeModel.generateContent(prompt)
            val aiResponseText = response.text

            if (aiResponseText == null) {
                Log.e("RecipeRepo", "AI 응답이 비어있습니다.")
                throw GeminiException.ParsingError()
            }

            Log.d("RecipeRepo", "AI 원본 응답: $aiResponseText")

            val jsonString = extractJsonString(aiResponseText)
            val recipeResponse = jsonParser.decodeFromString<AiRecipeResponse>(jsonString)
            val domainRecipe = recipeResponse.recipe.toDomainModel()

            val searchMetadata = RecipeSearchMetadata(
                ingredientsQuery = ingredientsQuery,
                timeFilter = safeTime,
                levelFilter = levelFilter,
                categoryFilter = safeCategory,
                utensilFilter = safeUtensil,
                useOnlySelected = useOnlySelected
            )

            val savedId = saveOrUpdateRecipe(domainRecipe, ingredientsQuery, searchMetadata)

            return domainRecipe.copy(
                id = savedId,
                searchMetadata = searchMetadata
            )
        } catch (e: Exception) {
            Log.e("RecipeRepo", "AI 레시피 호출 실패", e)
            throw mapToGeminiException(e)
        }
    }

    private suspend fun saveOrUpdateRecipe(
        domainRecipe: Recipe,
        ingredientsQuery: String,
        metadata: RecipeSearchMetadata
    ): Long {
        val existingEntity = recipeDao.findExistingRecipe(
            title = domainRecipe.title,
            ingredientsQuery = ingredientsQuery
        )

        return if (existingEntity != null) {
            Log.d("RecipeRepo", "기존 레시피 발견 (업데이트 안 함): ${domainRecipe.title} (ID: ${existingEntity.id})")
            existingEntity.id ?: throw IllegalStateException("기존 레시피의 ID가 누락되었습니다.")
        } else {
            Log.d("RecipeRepo", "새 레시피 삽입: ${domainRecipe.title}")
            val newRecipe = domainRecipe.copy(searchMetadata = metadata)
            recipeDao.insertRecipe(newRecipe.toEntity())
        }
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

    override fun getAllSavedRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes()
            .map { entityList ->
                entityList.map { it.toDomainModel() }
            }
    }

    override suspend fun getSavedRecipeById(id: Long): Recipe? {
        return recipeDao.getRecipeById(id)?.toDomainModel()
    }

    override suspend fun findRecipesByFilters(
        ingredientsQuery: String,
        timeFilter: String?,
        levelFilter: LevelType?,
        categoryFilter: String?,
        utensilFilter: String?,
        useOnlySelected: Boolean
    ): List<Recipe> {
        val entities = recipeDao.findRecipesByFilters(
            ingredientsQuery =  ingredientsQuery,
            timeFilter = sanitizeFilter(timeFilter),
            levelFilter = levelFilter?.label,
            categoryFilter = sanitizeFilter(categoryFilter),
            utensilFilter = sanitizeFilter(utensilFilter),
            useOnlySelected = useOnlySelected
        )
        return entities.map { it.toDomainModel() }
    }

    override suspend fun insertRecipe(recipe: Recipe) {
        recipeDao.insertRecipe(recipe.toEntity())
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        recipeDao.updateRecipe(recipe.toEntity())
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.deleteRecipe(recipe.toEntity())
    }

    override suspend fun deleteAllRecipes() {
        recipeDao.deleteAllRecipes()
    }
}