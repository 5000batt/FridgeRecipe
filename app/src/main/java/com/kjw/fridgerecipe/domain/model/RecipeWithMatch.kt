package com.kjw.fridgerecipe.domain.model

data class RecipeWithMatch(
    val recipe: Recipe,
    val matchCount: Int,
    val totalCount: Int,
    val matchPercentage: Int,
    val isCookable: Boolean,
    val missingIngredients: List<String>
) {
    companion object {
        fun from(recipe: Recipe, myIngredients: List<Ingredient>): RecipeWithMatch {
            // 내 재료 이름 정리 (검색 최적화를 위해 Set 사용)
            val myIngredientNames = myIngredients.map { it.name.trim() }.toSet()

            // 비교 대상 재료 선정 (필수 재료가 있으면 필수만, 없으면 전체)
            val targetIngredients = recipe.ingredients.filter { it.isEssential }.ifEmpty { recipe.ingredients }

            // 없는 재료 계산 (부분 일치 허용)
            val missingIngredients = targetIngredients.filter { recipeIngredient ->
                myIngredientNames.none { myName -> recipeIngredient.name.contains(myName) }
            }.map { it.name }

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
}
