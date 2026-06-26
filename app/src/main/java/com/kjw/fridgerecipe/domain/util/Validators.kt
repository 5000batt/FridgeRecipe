package com.kjw.fridgerecipe.domain.util

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.Recipe

fun Ingredient.validate(): DataResult<Unit> {
    if (name.isBlank()) return DataResult.Error(DataError.INGREDIENT_EMPTY_NAME)
    if (amount <= 0) return DataResult.Error(DataError.INGREDIENT_INVALID_AMOUNT)
    return DataResult.Success(Unit)
}

fun Recipe.validate(): DataResult<Unit> {
    if (title.isBlank()) return DataResult.Error(DataError.RECIPE_EMPTY_TITLE)
    if (servings <= 0) return DataResult.Error(DataError.RECIPE_INVALID_SERVINGS)
    if (time <= 0) return DataResult.Error(DataError.RECIPE_INVALID_TIME)
    if (ingredients.isEmpty()) return DataResult.Error(DataError.RECIPE_EMPTY_INGREDIENTS)
    if (ingredients.any { it.name.isBlank() || it.quantity.isBlank() })
        return DataResult.Error(DataError.RECIPE_INVALID_INGREDIENT_ITEM)
    if (steps.isEmpty()) return DataResult.Error(DataError.RECIPE_EMPTY_STEPS)
    if (steps.any { it.description.isBlank() })
        return DataResult.Error(DataError.RECIPE_INVALID_STEP_ITEM)
    return DataResult.Success(Unit)
}
