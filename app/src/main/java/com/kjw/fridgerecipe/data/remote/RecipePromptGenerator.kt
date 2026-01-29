package com.kjw.fridgerecipe.data.remote

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.model.CookingToolType
import javax.inject.Inject

class RecipePromptGenerator @Inject constructor() {

    fun createRecipePrompt(
        template: String,
        ingredients: List<Ingredient>,
        timeFilter: String?,
        levelFilter: LevelType?,
        categoryFilter: RecipeCategoryType?,
        cookingToolFilter: CookingToolType?,
        useOnlySelected: Boolean,
        excludedIngredients: List<String>
    ): String {
        val ingredientDetails = ingredients.joinToString(", ") { "${it.name} (${it.amount}${it.unit.label})" }

        val constraints = buildList {
            add("- 필수 재료: [$ingredientDetails]")
            timeFilter?.let { add("- 조리 시간: $it") }
            levelFilter?.let { add("- 난이도: ${it.id}") }
            categoryFilter?.let { add("- 음식 종류: ${it.id}") }
            cookingToolFilter?.let { add("- 조리 도구: ${it.id} (필수 사용)") }

            if (useOnlySelected) {
                add("- 추가 제약: 소금, 후추, 물 등 기본 양념 외에 '필수 재료' 목록에 없는 재료는 절대 사용 금지.")
            }
            if (excludedIngredients.isNotEmpty()) {
                add("- 제외 재료: [${excludedIngredients.joinToString(", ")}] (사용 금지)")
            }
        }.joinToString("\n")

        val safeTemplate = template.ifBlank { DEFAULT_PROMPT_TEMPLATE }

        return safeTemplate.replace("{{CONSTRAINTS}}", constraints)
    }

    companion object {
        const val DEFAULT_PROMPT_TEMPLATE = """
**Role**: 
당신은 제한된 재료로도 훌륭한 맛을 내는 창의적인 전문 셰프입니다.

**Goal**: 
제공된 [Constraint]를 엄격히 준수하여 최고의 레시피 1개를 추천해주세요.

**Constraint**:
{{CONSTRAINTS}}

**Format**:
반드시 아래 JSON 형식으로만 응답하세요. Markdown 코드 블록(```json)이나 사족을 달지 마세요.
난이도(level)는 반드시 BEGINNER, INTERMEDIATE, ADVANCED 중 하나로 응답하세요.

**Few-Shot Example**:
[Input]
재료: 김치, 참치
[Output]
{
  "recipe": { 
      "title": "참치 김치찌개",
      "info": { "servings": "1인분", "time": "20분", "level": "BEGINNER" },
      "ingredients": [ 
          { "name": "김치", "quantity": "1컵", "isEssential": true },
          { "name": "참치", "quantity": "1캔", "isEssential": true },
          { "name": "물", "quantity": "2컵", "isEssential": false } 
      ],
      "steps": [ { "number": 1, "description": "냄비에 김치를 볶습니다." } ]
  }
}
        """
    }
}
