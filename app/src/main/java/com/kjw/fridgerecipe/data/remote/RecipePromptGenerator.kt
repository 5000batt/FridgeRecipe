package com.kjw.fridgerecipe.data.remote

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import javax.inject.Inject

class RecipePromptGenerator @Inject constructor() {

    fun createRecipePrompt(
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
}