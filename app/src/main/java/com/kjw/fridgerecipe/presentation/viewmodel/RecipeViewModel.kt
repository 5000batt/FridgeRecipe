package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.usecase.GetRecommendedRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipeByIdUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val getRecommendedRecipeUseCase: GetRecommendedRecipeUseCase,
    private val getSavedRecipesUseCase: GetSavedRecipesUseCase,
    private val getSavedRecipeByIdUseCase: GetSavedRecipeByIdUseCase,
) : ViewModel() {

    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe.asStateFlow()
    private val _isRecipeLoading = MutableStateFlow(false)
    val isRecipeLoading: StateFlow<Boolean> = _isRecipeLoading.asStateFlow()

    private val _seenRecipeIds = MutableStateFlow<Set<Long>>(emptySet())
    private var currentIngredientsQuery: String = ""

    fun fetchRecipes(selectedIngredients: List<Ingredient>) {
        viewModelScope.launch {
            _isRecipeLoading.value = true
            try {
                val ingredientsQuery = selectedIngredients
                    .map { it.name }
                    .sorted()
                    .joinToString(",")

                if (ingredientsQuery != currentIngredientsQuery) {
                    _seenRecipeIds.value = emptySet()
                    currentIngredientsQuery = ingredientsQuery
                }

                val newRecipe = getRecommendedRecipeUseCase(selectedIngredients, _seenRecipeIds.value)

                _recipe.value = newRecipe

                newRecipe?.id?.let {
                    _seenRecipeIds.value = _seenRecipeIds.value + it
                }

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

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    fun toggleIngredientSelection(id: Long) {
        val currentIds = _selectedIds.value
        if (id in currentIds) {
            _selectedIds.value = currentIds - id
        } else {
            _selectedIds.value = currentIds + id
        }
    }
}