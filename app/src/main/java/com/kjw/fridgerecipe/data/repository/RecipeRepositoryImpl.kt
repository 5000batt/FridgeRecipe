package com.kjw.fridgerecipe.data.repository

import android.util.Log
import com.google.gson.Gson
import com.kjw.fridgerecipe.BuildConfig
import com.kjw.fridgerecipe.data.local.dao.RecipeDao
import com.kjw.fridgerecipe.data.remote.AiRecipeResponse
import com.kjw.fridgerecipe.data.remote.ApiService
import com.kjw.fridgerecipe.data.remote.GeminiRequest
import com.kjw.fridgerecipe.data.repository.mapper.toDomainModel
import com.kjw.fridgerecipe.data.repository.mapper.toEntity
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson,
    private val recipeDao: RecipeDao
) : RecipeRepository {

    override suspend fun getAiRecipes(
        ingredients: List<Ingredient>,
        ingredientsQuery: String,
        timeFilter: String?,
        levelFilter: LevelType?,
        categoryFilter: String?,
        utensilFilter: String?
    ): Recipe? {

        val ingredientDetails = ingredients.joinToString(", ") { "${it.name} (${it.amount}${it.unit.label})" }
        val constraints = buildList {
            add("필수 재료: [$ingredientDetails]")
            timeFilter?.let {
                add("조리 시간: $it")
            }
            levelFilter?.let {
                add("난이도: ${it.label}")
            }
            categoryFilter?.let {
                add("음식 종류: $it")
            }
            utensilFilter?.let {
                add("조리 도구: $it (필수 사용)")
            }
        }.joinToString("\n")

        val prompt = """
        다음 제약 조건에 맞는 음식 레시피 1개만 추천해줘.
        $constraints
        (만약 특정 조건이 '상관없음'이나 null이면, 그 조건은 자유롭게 결정해.)

        응답은 반드시 아래 JSON 형식과 정확히 일치해야 하며, 다른 설명이나 마크다운(` ``` `)을 포함하지 마.

        {
          "recipe": { 
              "title": "요리 이름",
              "info": { "servings": "X인분", "time": "X분", "level": "난이도" },
              "ingredients": [ { "name": "재료 1", "quantity": "수량 1" } ],
              "steps": [ { "number": 1, "description": "조리법 1" } ]
          }
        }
        """.trimIndent()

        Log.d("RecipeUseCase", "프롬프트 내용 : $prompt")

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
            val domainRecipe = recipeResponse.recipe.toDomainModel()
            val newId = recipeDao.insertRecipe(
                domainRecipe.copy(
                    ingredientsQuery = ingredientsQuery,
                    timeFilter = timeFilter,
                    levelFilter = levelFilter,
                    categoryFilter = categoryFilter,
                    utensilFilter = utensilFilter
                ).toEntity())

            return domainRecipe.copy(id = newId)
        } catch (e: Exception) {
            Log.e("RecipeRepo", "AI 레시피 호출 실패", e)
            null
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
        utensilFilter: String?
    ): List<Recipe> {
        val entities = recipeDao.findRecipesByFilters(
            ingredientsQuery =  ingredientsQuery,
            timeFilter = timeFilter,
            levelFilter = levelFilter?.label,
            categoryFilter = categoryFilter,
            utensilFilter = utensilFilter
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
}