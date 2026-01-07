package com.kjw.fridgerecipe.data.repository

import android.util.Log
import retrofit2.HttpException
import com.kjw.fridgerecipe.data.local.dao.RecipeDao
import com.kjw.fridgerecipe.data.remote.AiRecipeResponse
import com.kjw.fridgerecipe.data.remote.ApiService
import com.kjw.fridgerecipe.data.remote.GeminiRequest
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
import okio.IOException
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val recipeDao: RecipeDao,
    private val promptGenerator: RecipePromptGenerator
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
    private fun extractJsonOrThrow(text: String): String {
        val codeBlockRegex = Regex("```(?:json)?\\s*(\\{.*?\\})\\s*```", RegexOption.DOT_MATCHES_ALL)
        val matchResult = codeBlockRegex.find(text)

        if (matchResult != null) {
            return matchResult.groupValues[1]
        }

        val startIndex = text.indexOf('{')
        val endIndex = text.lastIndexOf('}')

        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            throw GeminiException.ParsingError()
        }

        return text.substring(startIndex, endIndex + 1)
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

        val geminiRequest = GeminiRequest(
            contents = listOf(GeminiRequest.Content(parts = listOf(GeminiRequest.Part(text = prompt))))
        )

        try {
            val geminiResponse = apiService.getGeminiRecipe(request = geminiRequest)
            var aiResponseText = geminiResponse.candidates?.firstOrNull()
                                    ?.content?.parts?.firstOrNull()?.text

            if (aiResponseText == null) {
                Log.e("RecipeRepo", "AI 응답이 비어있습니다.")
                throw GeminiException.ParsingError()
            }

            Log.d("RecipeRepo", "AI 원본 응답: $aiResponseText")

            val jsonString = extractJsonOrThrow(aiResponseText)

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

            val existingEntity = recipeDao.findExistingRecipe(
                title = domainRecipe.title,
                ingredientsQuery = ingredientsQuery
            )

            val savedId = if (existingEntity != null) {
                Log.d("RecipeRepo", "기존 레시피 발견 (업데이트 안 함): ${domainRecipe.title} (ID: ${existingEntity.id})")
                existingEntity.id
            } else {
                Log.d("RecipeRepo", "새 레시피 삽입: ${domainRecipe.title}")

                val newRecipe = domainRecipe.copy(
                    searchMetadata = searchMetadata
                )
                recipeDao.insertRecipe(newRecipe.toEntity())
            }

            return domainRecipe.copy(
                id = savedId,
                searchMetadata = searchMetadata
            )
        } catch (e: Exception) {
            Log.e("RecipeRepo", "AI 레시피 호출 실패", e)
            throw mapToGeminiException(e)
        }
    }

    private fun mapToGeminiException(e: Exception): GeminiException {
        return when (e) {
            is HttpException -> {
                when (e.code()) {
                    400 -> GeminiException.InvalidRequest()
                    401, 403 -> GeminiException.ApiKeyError()
                    429 -> GeminiException.QuotaExceeded()
                    503 -> GeminiException.ServerOverloaded()
                    else -> GeminiException.Unknown(e.code())
                }
            }
            is kotlinx.serialization.SerializationException,
            is IllegalStateException -> GeminiException.ParsingError()
            is IOException -> GeminiException.ServerOverloaded()
            else -> GeminiException.Unknown(-1)
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