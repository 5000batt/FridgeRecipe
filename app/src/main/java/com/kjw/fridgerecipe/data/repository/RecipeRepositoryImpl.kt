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
import com.kjw.fridgerecipe.domain.model.RecipeSearchMetadata
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import com.kjw.fridgerecipe.presentation.viewmodel.FILTER_ANY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson,
    private val recipeDao: RecipeDao
) : RecipeRepository {

    private fun sanitizeFilter(value: String?): String? {
        return if (value == FILTER_ANY || value.isNullOrBlank()) null else value
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

        val ingredientDetails = ingredients.joinToString(", ") { "${it.name} (${it.amount}${it.unit.label})" }

        val constraints = buildList {
            add("필수 재료: [$ingredientDetails]")
            safeTime?.let { add("조리 시간: $it") }
            levelFilter?.let { add("난이도: ${it.label}") }
            if (levelFilter == null) {
                add("난이도는 반드시 '초급', '중급', '고급' 중에서만 선택해.")
            }
            safeCategory?.let { add("음식 종류: $it") }
            safeUtensil?.let { add("조리 도구: $it (필수 사용)") }

            if (useOnlySelected) {
                add("제약: 소금, 후추, 물 같은 기본 양념을 제외하고, 명시된 '필수 재료' 외에 다른 재료는 절대 사용하지 마.")
            }
            if (excludedIngredients.isNotEmpty()) {
                val excludedString = excludedIngredients.joinToString(", ")
                add("제외 재료: [$excludedString] (이 재료들은 레시피에 절대 사용하지 마.")
            }
        }.joinToString("\n")

        val prompt = """
        다음 제약 조건에 맞는 음식 레시피 1개만 추천해줘.
        $constraints
        (만약 특정 조건이 '상관없음'이나 null이면, 그 조건은 자유롭게 결정해.)

        응답은 반드시 아래 JSON 형식과 정확히 일치해야 하며, 다른 설명이나 마크다운(` ``` `)을 포함하지 마.
        
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