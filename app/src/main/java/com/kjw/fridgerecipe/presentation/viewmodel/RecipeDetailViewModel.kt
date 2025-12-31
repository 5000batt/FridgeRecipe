package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.usecase.GetIngredientsUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipeByIdUseCase
import com.kjw.fridgerecipe.presentation.ui.model.RecipeWithMatch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val getSavedRecipeByIdUseCase: GetSavedRecipeByIdUseCase,
    private val getIngredientsUseCase: GetIngredientsUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _recipeId = MutableStateFlow<Long?>(null)
    val recipe: StateFlow<RecipeWithMatch?> = combine(
        _recipeId,
        getIngredientsUseCase()
    ) { recipeId, ingredients ->
        if (recipeId == null) {
            null
        } else {
            val recipe = getSavedRecipeByIdUseCase(recipeId)

            if (recipe == null) {
                null
            } else {
                val ingredientNames = ingredients.map { it.name.trim() }.toSet()
                val essentialIngredients = recipe.ingredients.filter { it.isEssential }
                val targetIngredients = if (essentialIngredients.isNotEmpty()) essentialIngredients else recipe.ingredients

                val missing = targetIngredients.filter { recipeIngredient ->
                    ingredientNames.none { myName -> recipeIngredient.name.contains(myName) }
                }.map { it.name }

                val totalCount = targetIngredients.size
                val matchCount = totalCount - missing.size
                val percentage = if (totalCount > 0) (matchCount * 100 / totalCount) else 0

                RecipeWithMatch(
                    recipe = recipe,
                    matchCount = matchCount,
                    totalCount = totalCount,
                    matchPercentage = percentage,
                    isCookable = percentage == 100,
                    missingIngredients = missing
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun loadRecipe(id: Long) {
        _recipeId.value = id
        _isLoading.value = false
    }

    fun clearRecipe() {
        _recipeId.value = null
        _isLoading.value = true
    }
}