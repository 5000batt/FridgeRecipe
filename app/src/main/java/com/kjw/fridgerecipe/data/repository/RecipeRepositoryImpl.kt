package com.kjw.fridgerecipe.data.repository

import android.util.Log
import retrofit2.HttpException
import com.google.gson.Gson
import com.kjw.fridgerecipe.BuildConfig
import com.kjw.fridgerecipe.data.local.dao.RecipeDao
import com.kjw.fridgerecipe.data.remote.AiRecipeResponse
import com.kjw.fridgerecipe.data.remote.ApiService
import com.kjw.fridgerecipe.data.remote.GeminiRequest
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
import okio.IOException
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson,
    private val recipeDao: RecipeDao
) : RecipeRepository {

    private fun sanitizeFilter(value: String?): String? {
        return if (value == FILTER_ANY || value.isNullOrBlank()) null else value
    }

    // Json 문자열만 추출
    private fun extractJsonOrThrow(text: String): String {
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

        val prompt = createRecipePrompt(
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

        val apiKey = BuildConfig.API_KEY

        try {
            val geminiResponse = apiService.getGeminiRecipe(apiKey = apiKey, request = geminiRequest)
            var aiResponseText = geminiResponse.candidates?.firstOrNull()
                                    ?.content?.parts?.firstOrNull()?.text

            if (aiResponseText == null) {
                Log.e("RecipeRepo", "AI 응답이 비어있습니다.")
                throw GeminiException.ParsingError()
            }

            Log.d("RecipeRepo", "AI 원본 응답: $aiResponseText")

            val jsonString = extractJsonOrThrow(aiResponseText)

            val recipeResponse = gson.fromJson(jsonString, AiRecipeResponse::class.java)
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

    private fun createRecipePrompt(
        ingredients: List<Ingredient>,
        timeFilter: String?,
        levelFilter: LevelType?,
        categoryFilter: String?,
        utensilFilter: String?,
        useOnlySelected: Boolean,
        excludedIngredients: List<String>
    ): String {
        val ingredientDetails = ingredients.joinToString(", ") { "${it.name} (${it.amount}${it.unit.label})" }

        val constraints = buildList {
            add("필수 재료: [$ingredientDetails]")
            timeFilter?.let { add("조리 시간: $it") }
            levelFilter?.let { add("난이도: ${it.label}") } ?: add("난이도는 '초급', '중급', '고급' 중 선택.")
            categoryFilter?.let { add("음식 종류: $it") }
            utensilFilter?.let { add("조리 도구: $it (필수 사용)") }

            if (useOnlySelected) {
                add("제약: 소금, 후추, 물 등 기본 양념 외에 '필수 재료' 목록에 없는 재료는 절대 사용 금지.")
            }
            if (excludedIngredients.isNotEmpty()) {
                add("제외 재료: [${excludedIngredients.joinToString(", ")}] (이 재료들은 절대 사용 금지).")
            }
        }.joinToString("\n")

        return """
            다음 제약 조건에 맞는 음식 레시피 1개만 추천해줘.
            $constraints
            (조건이 없거나 '상관없음'이면 AI가 알맞게 결정해.)

            응답은 반드시 아래 JSON 형식만 출력해 (마크다운, 설명 금지).
            
            [중요] ingredients 리스트에서, 위 '필수 재료' 목록에 포함된 재료를 사용했다면 "isEssential": true 로 설정하고, 소금/물 등 기본 양념이나 추가된 재료는 false로 설정해.

            {
              "recipe": { 
                  "title": "요리 이름",
                  "info": { "servings": "X인분", "time": "X분", "level": "난이도" },
                  "ingredients": [ 
                      { "name": "재료명", "quantity": "수량", "isEssential": true } 
                  ],
                  "steps": [ { "number": 1, "description": "조리법 1" } ]
              }
            }
        """.trimIndent()
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
            is com.google.gson.JsonSyntaxException,
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