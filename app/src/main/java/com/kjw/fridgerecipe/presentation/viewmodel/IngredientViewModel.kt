package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.usecase.AddIngredientUseCase
import com.kjw.fridgerecipe.domain.usecase.DelIngredientUseCase
import com.kjw.fridgerecipe.domain.usecase.GetIngredientByIdUseCase
import com.kjw.fridgerecipe.domain.usecase.GetIngredientsUseCase
import com.kjw.fridgerecipe.domain.usecase.UpdateIngredientUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IngredientViewModel @Inject constructor(
    private val addIngredientUseCase: AddIngredientUseCase,
    private val delIngredientUseCase: DelIngredientUseCase,
    getIngredientsUseCase: GetIngredientsUseCase,
    private val getIngredientByIdUseCase: GetIngredientByIdUseCase,
    private val updateIngredientUseCase: UpdateIngredientUseCase
    ) : ViewModel() {

    sealed class OperationResult {
        data class Success(val message: String) : OperationResult()
        data class Failure(val message: String) : OperationResult()
    }

    private val _operationResultEvent = MutableSharedFlow<OperationResult>()
    val operationResultEvent: SharedFlow<OperationResult> = _operationResultEvent.asSharedFlow()
    val ingredients: StateFlow<List<Ingredient>> = getIngredientsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedIngredient = MutableStateFlow<Ingredient?>(null)
    val selectedIngredient: StateFlow<Ingredient?> = _selectedIngredient.asStateFlow()

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
}