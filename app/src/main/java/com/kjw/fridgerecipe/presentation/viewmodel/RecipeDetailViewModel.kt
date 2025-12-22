package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipeByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val getSavedRecipeByIdUseCase: GetSavedRecipeByIdUseCase
) : ViewModel() {

    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe.asStateFlow()

    fun loadRecipe(id: Long) {
        viewModelScope.launch {
            _recipe.value = getSavedRecipeByIdUseCase(id)
        }
    }

    fun clearRecipe() {
        _recipe.value = null
    }
}