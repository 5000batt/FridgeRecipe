package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeWithMatch
import javax.inject.Inject

// 레시피와 사용자가 가진 재료 목록을 비교하여 일치 여부와 요리 가능 여부를 계산
class CalculateRecipeMatchUseCase @Inject constructor() {
    operator fun invoke(recipe: Recipe, myIngredients: List<Ingredient>): RecipeWithMatch {
        // 내 재료 이름 정리
        val ingredientNames = myIngredients.map { it.name.trim() }.toSet()

        // 비교 대상 재료 선정 (필수 재료가 없다면 전체 재료)
        val essentialIngredients = recipe.ingredients.filter { it.isEssential }
        val targetIngredients = essentialIngredients.ifEmpty { recipe.ingredients }

        // 없는 재료 계산
        val missingIngredients = targetIngredients.filter { recipeIngredient ->
            ingredientNames.none { myName -> recipeIngredient.name.contains(myName) }
        }.map { it.name }

        // 통계 계산
        val totalCount = targetIngredients.size
        val matchCount = totalCount - missingIngredients.size
        val percentage = if (totalCount > 0) (matchCount * 100 / totalCount) else 0

        return RecipeWithMatch(
            recipe = recipe,
            matchCount = matchCount,
            totalCount = totalCount,
            matchPercentage = percentage,
            isCookable = percentage == 100,
            missingIngredients = missingIngredients
        )
    }
}