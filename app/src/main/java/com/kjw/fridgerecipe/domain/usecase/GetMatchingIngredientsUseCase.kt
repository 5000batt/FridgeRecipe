package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import javax.inject.Inject

class GetMatchingIngredientsUseCase @Inject constructor(
    private val ingredientRepository: IngredientRepository
) {
    suspend operator fun invoke(recipe: Recipe): Map<String, Ingredient> {
        val fridgeIngredients = ingredientRepository.getAllIngredientsSuspend()
        val matchedMap = mutableMapOf<String, Ingredient>()
        val delimiters = charArrayOf(' ', '(', ')', '[', ']', ',', '/')

        recipe.ingredients.forEach { recipeIngredient ->
            val rName = recipeIngredient.name.trim()

            // 완전 일치 매칭
            var match = fridgeIngredients.find { it.name.trim() == rName }

            // Token 단위 매칭
            if (match == null) {
                match = fridgeIngredients.find { fridgeIngredient ->
                    val fName = fridgeIngredient.name.trim()
                    val rTokens = rName.split(*delimiters).filter { it.isNotBlank() }
                    val fTokens = fName.split(*delimiters).filter { it.isNotBlank() }

                    rTokens.contains(fName) || fTokens.contains(rName) ||
                            rTokens.any { rToken -> fTokens.any { fToken -> rToken == fToken } }
                }
            }

            // 단순 매칭
            if (match == null && rName.length > 1) {
                match = fridgeIngredients.find { fridgeIngredient ->
                    val fName = fridgeIngredient.name.trim()
                    fName.length > 1 && (rName.contains(fName) || fName.contains(rName))
                }
            }

            if (match != null) {
                matchedMap[recipeIngredient.name] = match
            }
        }
        return matchedMap
    }
}