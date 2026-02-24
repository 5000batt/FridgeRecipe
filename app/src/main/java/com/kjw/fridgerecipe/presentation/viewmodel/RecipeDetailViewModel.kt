package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.model.RecipeWithMatch
import com.kjw.fridgerecipe.domain.usecase.CalculateRecipeMatchUseCase
import com.kjw.fridgerecipe.domain.usecase.GetIngredientsUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipeByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel
    @Inject
    constructor(
        private val getSavedRecipeByIdUseCase: GetSavedRecipeByIdUseCase,
        private val getIngredientsUseCase: GetIngredientsUseCase,
        private val calculateRecipeMatchUseCase: CalculateRecipeMatchUseCase,
    ) : ViewModel() {
        private val _isLoading = MutableStateFlow(true)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        private val recipeId = MutableStateFlow<Long?>(null)
        val recipe: StateFlow<RecipeWithMatch?> =
            combine(
                recipeId,
                getIngredientsUseCase(),
            ) { recipeId, ingredients ->
                if (recipeId == null) {
                    null
                } else {
                    val recipeResult = getSavedRecipeByIdUseCase(recipeId)
                    val recipe = recipeResult.getOrNull()

                    if (recipe == null) {
                        null
                    } else {
                        calculateRecipeMatchUseCase(recipe, ingredients)
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

        fun loadRecipe(id: Long) {
            recipeId.value = id
            _isLoading.value = false
        }

        fun clearRecipe() {
            recipeId.value = null
            _isLoading.value = true
        }
    }
