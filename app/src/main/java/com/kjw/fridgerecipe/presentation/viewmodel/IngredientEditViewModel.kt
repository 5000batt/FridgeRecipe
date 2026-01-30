package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.IngredientCategoryType
import com.kjw.fridgerecipe.domain.model.IngredientIcon
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.domain.model.UnitType
import com.kjw.fridgerecipe.domain.usecase.InsertIngredientUseCase
import com.kjw.fridgerecipe.domain.usecase.DelIngredientUseCase
import com.kjw.fridgerecipe.domain.usecase.GetIngredientByIdUseCase
import com.kjw.fridgerecipe.domain.usecase.UpdateIngredientUseCase
import com.kjw.fridgerecipe.domain.util.DataResult
import com.kjw.fridgerecipe.presentation.ui.model.IngredientEditUiState
import com.kjw.fridgerecipe.presentation.ui.model.OperationResult
import com.kjw.fridgerecipe.presentation.ui.model.IngredientValidationField
import com.kjw.fridgerecipe.presentation.util.UiText
import com.kjw.fridgerecipe.presentation.validator.IngredientValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class IngredientEditViewModel @Inject constructor(
    private val getIngredientByIdUseCase: GetIngredientByIdUseCase,
    private val insertIngredientUseCase: InsertIngredientUseCase,
    private val updateIngredientUseCase: UpdateIngredientUseCase,
    private val delIngredientUseCase: DelIngredientUseCase,
    private val validator: IngredientValidator
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _operationResultEvent = MutableSharedFlow<OperationResult>()
    val operationResultEvent: SharedFlow<OperationResult> = _operationResultEvent.asSharedFlow()

    private val _navigationEvent = MutableSharedFlow<Unit>()
    val navigationEvent: SharedFlow<Unit> = _navigationEvent.asSharedFlow()

    private val _validationEvent = MutableSharedFlow<IngredientValidationField>()
    val validationEvent: SharedFlow<IngredientValidationField> = _validationEvent.asSharedFlow()

    private val _editUiState = MutableStateFlow(IngredientEditUiState())
    val editUiState: StateFlow<IngredientEditUiState> = _editUiState.asStateFlow()

    private var editingIngredient: Ingredient? = null

    private val decimalRegex = Regex("^\\d*\\.?\\d{0,2}\$")

    fun loadIngredientById(id: Long) {
        viewModelScope.launch {
            val result = getIngredientByIdUseCase(id)
            if (result is DataResult.Success) {
                val ingredient = result.data
                editingIngredient = ingredient
                _editUiState.value = ingredient.toEditUiState()
            } else {
                editingIngredient = null
                _editUiState.value = IngredientEditUiState()
            }
            _isLoading.value = false
        }
    }

    fun onReadyToDisplay() {
        _isLoading.value = false
    }

    fun clearState() {
        editingIngredient = null
        _editUiState.value = IngredientEditUiState()
        _isLoading.value = false
    }

    fun onNameChanged(name: String) {
        _editUiState.update { it.copy(name = name, nameError = null) }
    }

    fun onAmountChanged(amount: String) {
        if (amount.isEmpty() || decimalRegex.matches(amount)) {
            _editUiState.update { it.copy(amount = amount, amountError = null) }
        }
    }

    fun onUnitChanged(unit: UnitType) {
        _editUiState.update { it.copy(selectedUnit = unit) }
    }

    fun onStorageChanged(storage: StorageType) {
        _editUiState.update { it.copy(selectedStorage = storage) }
    }

    fun onCategoryChanged(category: IngredientCategoryType) {
        _editUiState.update { it.copy(selectedCategory = category) }
    }

    fun onDateSelected(date: LocalDate) {
        _editUiState.update { it.copy(selectedDate = date, showDatePicker = false) }
    }

    fun onIconCategorySelected(category: IngredientCategoryType?) {
        _editUiState.update { it.copy(selectedIconCategory = category) }
    }

    fun onIconSelected(icon: IngredientIcon) {
        _editUiState.update { it.copy(selectedIcon = icon) }
    }

    fun onDatePickerDialogShow() {
        _editUiState.update { it.copy(showDatePicker = true) }
    }

    fun onDatePickerDialogDismiss() {
        _editUiState.update { it.copy(showDatePicker = false) }
    }

    fun onDeleteDialogShow() {
        val name = editingIngredient?.name
        _editUiState.update { it.copy(showDeleteDialog = true, selectedIngredientName = name) }
    }

    fun onDeleteDialogDismiss() {
        _editUiState.update { it.copy(showDeleteDialog = false) }
    }

    private fun updateErrorState(failure: IngredientValidator.ValidationResult.Failure) {
        _editUiState.update { state ->
            when (failure.field) {
                IngredientValidationField.NAME -> state.copy(nameError = failure.errorMessage)
                IngredientValidationField.AMOUNT -> state.copy(amountError = failure.errorMessage)
            }
        }
    }

    fun onSaveOrUpdateIngredient(isEditMode: Boolean) {
        viewModelScope.launch {
            val validationResult = validator.validate(_editUiState.value)
            if (validationResult is IngredientValidator.ValidationResult.Failure) {
                updateErrorState(validationResult)
                _validationEvent.emit(validationResult.field)
                return@launch
            }

            val currentState = _editUiState.value

            val ingredientToSave = Ingredient(
                id = if (isEditMode) editingIngredient?.id else null,
                name = currentState.name,
                amount = currentState.amount.toDoubleOrNull() ?: 0.0,
                unit = currentState.selectedUnit,
                expirationDate = currentState.selectedDate,
                storageLocation = currentState.selectedStorage,
                category = currentState.selectedCategory,
                emoticon = currentState.selectedIcon
            )

            val result = if (isEditMode) {
                updateIngredientUseCase(ingredientToSave)
            } else {
                insertIngredientUseCase(ingredientToSave)
            }

            when (result) {
                is DataResult.Success -> {
                    val messageResId = if (isEditMode) R.string.msg_updated else R.string.msg_saved
                    _operationResultEvent.emit(OperationResult.Success(UiText.StringResource(messageResId)))
                    _navigationEvent.emit(Unit)
                }
                is DataResult.Error -> {
                    _operationResultEvent.emit(OperationResult.Failure(result.message))
                }
                else -> Unit
            }
        }
    }

    fun onDeleteIngredient() {
        viewModelScope.launch {
            editingIngredient?.let {
                val result = delIngredientUseCase(it)
                when (result) {
                    is DataResult.Success -> {
                        _operationResultEvent.emit(OperationResult.Success(UiText.StringResource(R.string.msg_deleted)))
                        _navigationEvent.emit(Unit)
                    }
                    is DataResult.Error -> {
                        _operationResultEvent.emit(OperationResult.Failure(result.message))
                    }
                    else -> Unit
                }
            }
            _editUiState.update { it.copy(showDeleteDialog = false) }
        }
    }

    private fun Ingredient.toEditUiState(): IngredientEditUiState {
        val amountString = if (this.amount % 1.0 == 0.0) {
            this.amount.toInt().toString()
        } else {
            this.amount.toString()
        }

        return IngredientEditUiState(
            name = this.name,
            amount = amountString,
            selectedUnit = this.unit,
            selectedDate = this.expirationDate,
            selectedStorage = this.storageLocation,
            selectedCategory = this.category,
            selectedIcon = this.emoticon,
            selectedIconCategory = this.emoticon.category
        )
    }
}
