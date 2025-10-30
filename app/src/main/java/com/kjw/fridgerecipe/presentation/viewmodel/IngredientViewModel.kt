package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.usecase.AddIngredientUseCase
import com.kjw.fridgerecipe.domain.usecase.DelIngredientUseCase
import com.kjw.fridgerecipe.domain.usecase.GetIngredientsUseCase
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
    getIngredientsUseCase: GetIngredientsUseCase
    ) : ViewModel() {

    sealed class AddResult {
        data class Success(val message: String) : AddResult()
        data class Failure(val message: String) : AddResult()
    }

    private val _addResultEvent = MutableSharedFlow<AddResult>()
    val addResultEvent: SharedFlow<AddResult> = _addResultEvent.asSharedFlow()

    val ingredients: StateFlow<List<Ingredient>> = getIngredientsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            val success = addIngredientUseCase(ingredient)
            if (success) {
                _addResultEvent.emit(AddResult.Success("저장되었습니다."))
            } else {
                _addResultEvent.emit(AddResult.Failure("저장에 실패했습니다."))
            }
        }
    }

    fun delIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            delIngredientUseCase(ingredient)
        }
    }
}