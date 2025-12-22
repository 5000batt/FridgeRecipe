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
import com.kjw.fridgerecipe.domain.usecase.GetIngredientsUseCase
import com.kjw.fridgerecipe.domain.usecase.UpdateIngredientUseCase
import com.kjw.fridgerecipe.presentation.ui.model.OperationResult
import com.kjw.fridgerecipe.presentation.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class IngredientEditUiState(
    val name: String = "",
    val nameError: UiText? = null,
    val amount: String = "",
    val amountError: UiText? = null,
    val selectedUnit: UnitType = UnitType.COUNT,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedStorage: StorageType = StorageType.REFRIGERATED,
    val selectedCategory: CategoryType = CategoryType.ETC,
    val showDatePicker: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val selectedIngredientName: String? = null,
    val selectedIcon: IngredientIcon = IngredientIcon.DEFAULT,
    val selectedIconCategory: CategoryType? = null,
)

enum class ValidationField {
    NAME, AMOUNT
}

@HiltViewModel
class IngredientViewModel @Inject constructor(
    private val insertIngredientUseCase: InsertIngredientUseCase,
    private val delIngredientUseCase: DelIngredientUseCase,
    getIngredientsUseCase: GetIngredientsUseCase,
    private val getIngredientByIdUseCase: GetIngredientByIdUseCase,
    private val updateIngredientUseCase: UpdateIngredientUseCase
    ) : ViewModel() {

    companion object {
        private val AMOUNT_REGEX = Regex("^\\d*\\.?\\d{0,2}\$")
    }

    private val _operationResultEvent = MutableSharedFlow<OperationResult>()
    val operationResultEvent: SharedFlow<OperationResult> = _operationResultEvent.asSharedFlow()
    private val _rawIngredientsFlow = getIngredientsUseCase()
        .onEach { _isLoading.value = false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // HomeScreen States
    val allIngredients: StateFlow<List<Ingredient>> = _rawIngredientsFlow
    val homeScreenIngredients: StateFlow<Map<StorageType, List<Ingredient>>> =
        _rawIngredientsFlow.map { ingredients ->
            ingredients.groupBy { it.storageLocation }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // IngredientListScreen States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val categorizedIngredients: StateFlow<Map<CategoryType, List<Ingredient>>> =
        _searchQuery.combine(_rawIngredientsFlow) { query, ingredients ->
            val filteredList = if (query.isBlank()) {
                ingredients
            } else {
                ingredients.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            }
            filteredList.groupBy { it.category }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // IngredientEditScreen States
    private val _selectedIngredient = MutableStateFlow<Ingredient?>(null)
    val selectedIngredient: StateFlow<Ingredient?> = _selectedIngredient.asStateFlow()
    private val _editUiState = MutableStateFlow(IngredientEditUiState())
    val editUiState: StateFlow<IngredientEditUiState> = _editUiState.asStateFlow()

    private val _validationEvent = MutableSharedFlow<ValidationField>()
    val validationEvent: SharedFlow<ValidationField> = _validationEvent.asSharedFlow()

    // IngredientListScreen Function
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    // IngredientEditScreen Functions
    fun loadIngredientById(id: Long) {
        viewModelScope.launch {
            val ingredient = getIngredientByIdUseCase(id)
            _selectedIngredient.value = ingredient
            _editUiState.value = ingredient?.toEditUiState() ?: IngredientEditUiState()
        }
    }

    fun clearSelectedIngredient() {
        _selectedIngredient.value = null
        _editUiState.value = IngredientEditUiState()
    }

    fun onIconSelected(icon: IngredientIcon) {
        _editUiState.update { state ->
            val newState = state.copy(selectedIcon = icon)

            if (icon != IngredientIcon.DEFAULT) {
                newState.copy(selectedCategory = icon.category)
            } else {
                newState
            }
        }
    }

    fun onIconCategorySelected(category: CategoryType) {
        _editUiState.update {
            val newCategory = if (it.selectedIconCategory == category) null else category
            it.copy(selectedIconCategory = newCategory)
        }
    }

    fun onNameChanged(newName: String) {
        _editUiState.update { it.copy(name = newName, nameError = null) }
    }

    fun onAmountChanged(newAmount: String) {
        if (newAmount.isEmpty() || newAmount.matches(AMOUNT_REGEX)) {
            _editUiState.update { it.copy(amount = newAmount, amountError = null) }
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

    fun onDatePickerDialogShow() {
        _editUiState.update { it.copy(showDatePicker = true) }
    }

    fun onDatePickerDialogDismiss() {
        _editUiState.update { it.copy(showDatePicker = false) }
    }

    fun onDeleteDialogShow() {
        _editUiState.update { it.copy(showDeleteDialog = true, selectedIngredientName = _selectedIngredient.value?.name) }
    }

    fun onDeleteDialogDismiss() {
        _editUiState.update { it.copy(showDeleteDialog = false) }
    }

    private suspend fun validateInputs(): Boolean {
        val currentState = _editUiState.value
        val amountDouble = currentState.amount.toDoubleOrNull()

        if (currentState.name.isBlank()) {
            _editUiState.update {
                it.copy(nameError = UiText.StringResource(R.string.error_validation_name_empty))
            }
            _validationEvent.emit(ValidationField.NAME)
            return false
        }
        if (amountDouble == null || amountDouble <= 0) {
            val errorMsg = if (amountDouble == null) {
                UiText.StringResource(R.string.error_validation_amount_empty)
            } else {
                UiText.StringResource(R.string.error_validation_amount_zero)
            }

            _editUiState.update { it.copy(amountError = errorMsg) }
            _validationEvent.emit(ValidationField.AMOUNT)
            return false
        }

        return true
    }

    private fun buildIngredientFromState(isEditMode: Boolean): Ingredient {
        val currentState = _editUiState.value
        val safeAmount = currentState.amount.toDoubleOrNull() ?: 0.0

        return Ingredient(
            id = if (isEditMode) _selectedIngredient.value?.id else null,
            name = currentState.name.trim(),
            amount = safeAmount,
            unit = currentState.selectedUnit,
            expirationDate = currentState.selectedDate,
            storageLocation = currentState.selectedStorage,
            category = currentState.selectedCategory,
            emoticon = currentState.selectedIcon
        )
    }

    fun onSaveOrUpdateIngredient(isEditMode: Boolean) {
        viewModelScope.launch {
            if (!validateInputs()) {
                return@launch
            }

            val ingredientToSave = buildIngredientFromState(isEditMode)

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
            _selectedIngredient.value?.let {
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
}

private fun Ingredient.toEditUiState(): IngredientEditUiState {
    return IngredientEditUiState(
        name = this.name,
        amount = this.amount.toString(),
        selectedUnit = this.unit,
        selectedDate = this.expirationDate,
        selectedStorage = this.storageLocation,
        selectedCategory = this.category,
        selectedIcon = this.emoticon,
        selectedIconCategory = this.emoticon.category
    )
}