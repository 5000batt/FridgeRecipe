package com.kjw.fridgerecipe.domain.usecase

import android.util.Log
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import javax.inject.Inject

class GetRecommendedRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(
        ingredients: List<Ingredient>,
        seenIds: Set<Long>
    ): Recipe? {

        val ingredientsQuery = ingredients
            .map { it.name }
            .sorted()
            .joinToString(",")

        val cashedList = recipeRepository.findSavedRecipeByQuery(ingredientsQuery)

        if (cashedList.isNotEmpty()) {
            val availableCache = cashedList.filter { it.id !in seenIds }

            if (availableCache.isNotEmpty()) {
                Log.d("RecipeUseCase", "캐시된 목록 (다른 레시피) 반환")
                return availableCache.random()
            }
        }

        Log.d("RecipeUseCase", "AI 호출 (캐시 없음 또는 모두 순회)")

        val ingredientDetails = ingredients.joinToString(", ") { "${it.name} (${it.amount}${it.unit.label})" }
        val prompt = """
        재료: [$ingredientDetails]. 이 재료들로 만들 수 있는 음식 레시피 1개만 추천해줘.
        응답은 반드시 아래 JSON 형식과 정확히 일치해야 하며, 다른 설명이나 마크다운(` ``` `)을 포함하지 마.

        {
          "recipe": { 
              "title": "요리 이름",
              "info": { "servings": "X인분", "time": "X분", "level": "초급" },
              "ingredients": [ { "name": "재료 1", "quantity": "수량 1" } ],
              "steps": [ { "number": 1, "description": "조리법 1" } ]
          }
        }
        """.trimIndent()

        return recipeRepository.getAiRecipes(prompt, ingredientsQuery)
    }
}