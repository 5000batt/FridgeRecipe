package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.CategoryType
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.IngredientIcon
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.domain.model.UnitType
import com.kjw.fridgerecipe.domain.usecase.InsertIngredientUseCase
import com.kjw.fridgerecipe.domain.usecase.DelIngredientUseCase
import com.kjw.fridgerecipe.domain.usecase.GetIngredientByIdUseCase
import com.kjw.fridgerecipe.domain.usecase.UpdateIngredientUseCase
import com.kjw.fridgerecipe.presentation.ui.model.IngredientEditUiState
import com.kjw.fridgerecipe.presentation.ui.model.OperationResult
import com.kjw.fridgerecipe.presentation.ui.model.IngredientValidationField
import com.kjw.fridgerecipe.presentation.util.UiText
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
    private val delIngredientUseCase: DelIngredientUseCase
    ) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _operationResultEvent = MutableSharedFlow<OperationResult>()
    val operationResultEvent: SharedFlow<OperationResult> = _operationResultEvent.asSharedFlow()

    private val _validationEvent = MutableSharedFlow<IngredientValidationField>()
    val validationEvent: SharedFlow<IngredientValidationField> = _validationEvent.asSharedFlow()

    private val _editUiState = MutableStateFlow(IngredientEditUiState())
    val editUiState: StateFlow<IngredientEditUiState> = _editUiState.asStateFlow()

    private var editingIngredient: Ingredient? = null

    private val decimalRegex = Regex("^\\d*\\.?\\d{0,2}\$")


    fun loadIngredientById(id: Long) {
        viewModelScope.launch {
            val ingredient = getIngredientByIdUseCase(id)
            editingIngredient = ingredient
            _editUiState.value = ingredient?.toEditUiState() ?: IngredientEditUiState()
            if (ingredient == null) {
                _isLoading.value = false
            }
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

    fun onCategoryChanged(category: CategoryType) {
        _editUiState.update { it.copy(selectedCategory = category) }
    }

    fun onDateSelected(date: LocalDate) {
        _editUiState.update { it.copy(selectedDate = date, showDatePicker = false) }
    }

    fun onIconCategorySelected(category: CategoryType?) {
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

    private suspend fun validateInputs(): Boolean {
        val currentState = _editUiState.value
        var isValid = true

        if (currentState.name.isBlank()) {
            _editUiState.update { it.copy(nameError = UiText.StringResource(R.string.error_validation_name_empty)) }
            _validationEvent.emit(IngredientValidationField.NAME)
            isValid = false
        }

        if (currentState.amount.isBlank()) {
            _editUiState.update { it.copy(amountError = UiText.StringResource(R.string.error_validation_amount_empty)) }
            if (isValid) _validationEvent.emit(IngredientValidationField.AMOUNT) // 첫 번째 에러 필드로 포커스 이동을 위해
            isValid = false
        }

        return isValid
    }

    fun onSaveOrUpdateIngredient(isEditMode: Boolean) {
        viewModelScope.launch {
            if (!validateInputs()) return@launch

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

            val success = if (isEditMode) {
                updateIngredientUseCase(ingredientToSave)
            } else {
                insertIngredientUseCase(ingredientToSave)
            }

            if (success) {
                val messageResId = if (isEditMode) R.string.msg_updated else R.string.msg_saved
                _operationResultEvent.emit(OperationResult.Success(UiText.StringResource(messageResId)))
            } else {
                val messageResId = if (isEditMode) R.string.error_update_failed else R.string.error_save_failed
                _operationResultEvent.emit(OperationResult.Failure(UiText.StringResource(messageResId)))
            }
        }
    }

    fun onDeleteIngredient() {
        viewModelScope.launch {
            editingIngredient?.let {
                val success = delIngredientUseCase(it)
                if (success) {
                    _operationResultEvent.emit(OperationResult.Success(UiText.StringResource(R.string.msg_deleted)))
                } else {
                    _operationResultEvent.emit(OperationResult.Failure(UiText.StringResource(R.string.error_delete_failed)))
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