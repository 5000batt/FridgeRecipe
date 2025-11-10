package com.kjw.fridgerecipe.domain.usecase

import android.util.Log
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import javax.inject.Inject

class GetRecommendedRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(
        ingredients: List<Ingredient>,
        seenIds: Set<Long>,
        timeFilter: String?,
        levelFilter: LevelType?,
        categoryFilter: String?,
        utensilFilter: String?
    ): Recipe? {

        val ingredientsQuery = ingredients
            .map { it.name }
            .sorted()
            .joinToString(",")

        val cashedList = recipeRepository.findRecipesByFilters(
            ingredientsQuery = ingredientsQuery,
            timeFilter = timeFilter,
            levelFilter = levelFilter,
            categoryFilter = categoryFilter,
            utensilFilter = utensilFilter
        )

        if (cashedList.isNotEmpty()) {
            val availableCache = cashedList.filter { it.id !in seenIds }

            if (availableCache.isNotEmpty()) {
                Log.d("RecipeUseCase", "캐시된 목록 (다른 레시피) 반환")
                return availableCache.random()
            }
        }

        Log.d("RecipeUseCase", "AI 호출 (캐시 없음 또는 모두 순회)")

        /*val ingredientDetails = ingredients.joinToString(", ") { "${it.name} (${it.amount}${it.unit.label})" }
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

        Log.d("RecipeUseCase", "프롬프트 내용 : $prompt")*/

        return recipeRepository.getAiRecipes(
            ingredients = ingredients,
            ingredientsQuery = ingredientsQuery,
            timeFilter = timeFilter,
            levelFilter = levelFilter,
            categoryFilter = categoryFilter,
            utensilFilter = utensilFilter
        )
    }
}