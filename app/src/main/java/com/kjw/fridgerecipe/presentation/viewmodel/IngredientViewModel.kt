package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.usecase.AddIngredientUseCase
import com.kjw.fridgerecipe.domain.usecase.DelIngredientUseCase
import com.kjw.fridgerecipe.domain.usecase.GetIngredientByIdUseCase
import com.kjw.fridgerecipe.domain.usecase.GetIngredientsUseCase
import com.kjw.fridgerecipe.domain.usecase.GetRecommendedRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipeByIdUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipesUseCase
import com.kjw.fridgerecipe.domain.usecase.UpdateIngredientUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IngredientViewModel @Inject constructor(
    private val addIngredientUseCase: AddIngredientUseCase,
    private val delIngredientUseCase: DelIngredientUseCase,
    getIngredientsUseCase: GetIngredientsUseCase,
    private val getIngredientByIdUseCase: GetIngredientByIdUseCase,
    private val updateIngredientUseCase: UpdateIngredientUseCase,
    private val getRecommendedRecipeUseCase: GetRecommendedRecipeUseCase,
    private val getSavedRecipesUseCase: GetSavedRecipesUseCase,
    private val getSavedRecipeByIdUseCase: GetSavedRecipeByIdUseCase
    ) : ViewModel() {

    sealed class OperationResult {
        data class Success(val message: String) : OperationResult()
        data class Failure(val message: String) : OperationResult()
    }

    private val _operationResultEvent = MutableSharedFlow<OperationResult>()
    val operationResultEvent: SharedFlow<OperationResult> = _operationResultEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val ingredients: StateFlow<List<Ingredient>> = getIngredientsUseCase()
        .onEach {
            _isLoading.value = false
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _selectedIngredient = MutableStateFlow<Ingredient?>(null)
    val selectedIngredient: StateFlow<Ingredient?> = _selectedIngredient.asStateFlow()

    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe.asStateFlow()
    private val _isRecipeLoading = MutableStateFlow(false)
    val isRecipeLoading: StateFlow<Boolean> = _isRecipeLoading.asStateFlow()

    fun loadIngredient(id: Long) {
        viewModelScope.launch {
            _selectedIngredient.value = getIngredientByIdUseCase(id)
        }
    }

    fun clearSelectedIngredient() {
        _selectedIngredient.value = null
    }

    fun addIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            val success = addIngredientUseCase(ingredient)
            if (success) {
                _operationResultEvent.emit(OperationResult.Success("저장되었습니다."))
            } else {
                _operationResultEvent.emit(OperationResult.Failure("저장에 실패했습니다."))
            }
        }
    }

    fun updateIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            val success = updateIngredientUseCase(ingredient)
            if (success) {
                _operationResultEvent.emit(OperationResult.Success("수정되었습니다."))
            } else {
                _operationResultEvent.emit(OperationResult.Failure("수정에 실패했습니다."))
            }
        }
    }

    fun delIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            val success = delIngredientUseCase(ingredient)
            if (success) {
                _operationResultEvent.emit(OperationResult.Success("삭제되었습니다."))
            } else {
                _operationResultEvent.emit(OperationResult.Failure("삭제에 실패했습니다."))
            }
        }
    }

    fun fetchRecipes() {
        viewModelScope.launch {
            _isRecipeLoading.value = true
            try {
                val currentIngredient = ingredients.value
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