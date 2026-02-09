package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeWithMatch
import com.kjw.fridgerecipe.domain.util.RecipeMatcher
import javax.inject.Inject

/**
 * 레시피와 사용자가 가진 재료 목록을 비교하여 일치 여부와 요리 가능 여부를 계산하는 유스케이스.
 * 실제 계산 로직은 [RecipeMatcher]를 통해 처리됩니다.
 */
class CalculateRecipeMatchUseCase @Inject constructor(
    private val recipeMatcher: RecipeMatcher
) {
    suspend operator fun invoke(recipe: Recipe, myIngredients: List<Ingredient>): RecipeWithMatch {
        return recipeMatcher.calculateMatch(recipe, myIngredients)
    }
}
