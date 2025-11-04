package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.usecase.GetIngredientsUseCase
import com.kjw.fridgerecipe.domain.usecase.GetRecommendedRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipeByIdUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val getRecommendedRecipeUseCase: GetRecommendedRecipeUseCase,
    private val getSavedRecipesUseCase: GetSavedRecipesUseCase,
    private val getSavedRecipeByIdUseCase: GetSavedRecipeByIdUseCase,
    private val getIngredientsUseCase: GetIngredientsUseCase
) : ViewModel() {

    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe.asStateFlow()
    private val _isRecipeLoading = MutableStateFlow(false)
    val isRecipeLoading: StateFlow<Boolean> = _isRecipeLoading.asStateFlow()

    fun fetchRecipes() {
        viewModelScope.launch {
            _isRecipeLoading.value = true
            try {
                val currentIngredient = getIngredientsUseCase().first()
                _recipe.value = getRecommendedRecipeUseCase(currentIngredient)
            } finally {
                _isRecipeLoading.value = false
            }
        }
    }

    val savedRecipes: StateFlow<List<Recipe>> = getSavedRecipesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()

    fun loadRecipeById(id: Long) {
        viewModelScope.launch {
            _selectedRecipe.value = getSavedRecipeByIdUseCase(id)
        }
    }

    fun clearSelectedRecipe() {
        _selectedRecipe.value = null
    }
}