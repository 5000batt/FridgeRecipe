package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.kjw.fridgerecipe.presentation.ui.common.OperationResult
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

data class IngredientDetailUiState(
    val name: String = "",
    val nameError: String? = null,
    val amount: String = "",
    val amountError: String? = null,
    val selectedUnit: UnitType = UnitType.COUNT,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedStorage: StorageType = StorageType.REFRIGERATED,
    val selectedCategory: CategoryType = CategoryType.ETC,
    val unitExpanded: Boolean = false,
    val storageExpanded: Boolean = false,
    val categoryExpanded: Boolean = false,
    val showDatePicker: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val selectedIngredientName: String? = null
)

@HiltViewModel
class IngredientViewModel @Inject constructor(
    private val insertIngredientUseCase: InsertIngredientUseCase,
    private val delIngredientUseCase: DelIngredientUseCase,
    getIngredientsUseCase: GetIngredientsUseCase,
    private val getIngredientByIdUseCase: GetIngredientByIdUseCase,
    private val updateIngredientUseCase: UpdateIngredientUseCase
    ) : ViewModel() {

    private val _operationResultEvent = MutableSharedFlow<OperationResult>()
    val operationResultEvent: SharedFlow<OperationResult> = _operationResultEvent.asSharedFlow()

    // IngredientListScreen States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _rawIngredientsFlow = getIngredientsUseCase()
        .onEach { _isLoading.value = false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

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

    // IngredientListScreen Function
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

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

    // IngredientDetailScreen States
    private val _selectedIngredient = MutableStateFlow<Ingredient?>(null)
    val selectedIngredient: StateFlow<Ingredient?> = _selectedIngredient.asStateFlow()
    private val _detailUiState = MutableStateFlow(IngredientDetailUiState())
    val detailUiState: StateFlow<IngredientDetailUiState> = _detailUiState.asStateFlow()

    // IngredientDetailScreen Functions
    fun loadIngredientById(id: Long) {
        viewModelScope.launch {
            val ingredient = getIngredientByIdUseCase(id)
            _selectedIngredient.value = ingredient
            _detailUiState.value = ingredient?.toDetailUiState() ?: IngredientDetailUiState()
        }
    }

    fun clearSelectedIngredient() {
        _selectedIngredient.value = null
        _detailUiState.value = IngredientDetailUiState()
    }

    fun onNameChanged(newName: String) {
        _detailUiState.update { it.copy(name = newName, nameError = null) }
    }

    fun onAmountChanged(newAmount: String) {
        val regex = Regex("^\\d*\\.?\\d{0,2}\$")
        if (newAmount.isEmpty() || newAmount.matches(regex)) {
            _detailUiState.update { it.copy(amount = newAmount, amountError = null) }
        }
    }

    fun onUnitChanged(unit: UnitType) {
        _detailUiState.update { it.copy(selectedUnit = unit, unitExpanded = false) }
    }

    fun onUnitMenuExpandedChanged(isExpanded: Boolean) {
        _detailUiState.update { it.copy(unitExpanded = isExpanded) }
    }

    fun onStorageChanged(storage: StorageType) {
        _detailUiState.update { it.copy(selectedStorage = storage, storageExpanded = false) }
    }

    fun onStorageMenuExpandedChanged(isExpanded: Boolean) {
        _detailUiState.update { it.copy(storageExpanded = isExpanded) }
    }

    fun onCategoryChanged(category: CategoryType) {
        _detailUiState.update { it.copy(selectedCategory = category, categoryExpanded = false) }
    }

    fun onCategoryMenuExpandedChanged(isExpanded: Boolean) {
        _detailUiState.update { it.copy(categoryExpanded = isExpanded) }
    }

    fun onDateSelected(date: LocalDate) {
        _detailUiState.update { it.copy(selectedDate = date, showDatePicker = false) }
    }

    fun onDatePickerDialogShow() {
        _detailUiState.update { it.copy(showDatePicker = true) }
    }

    fun onDatePickerDialogDismiss() {
        _detailUiState.update { it.copy(showDatePicker = false) }
    }

    fun onDeleteDialogShow() {
        _detailUiState.update { it.copy(showDeleteDialog = true, selectedIngredientName = _selectedIngredient.value?.name) }
    }

    fun onDeleteDialogDismiss() {
        _detailUiState.update { it.copy(showDeleteDialog = false) }
    }

    private fun validateInputs(): Boolean {
        val currentState = _detailUiState.value
        val amountDouble = currentState.amount.toDoubleOrNull()
        var isValid = true

        if (currentState.name.isBlank()) {
            _detailUiState.update { it.copy(nameError = "재료 이름을 입력해주세요.") }
            isValid = false
        }
        if (amountDouble == null || amountDouble <= 0) {
            val errorMsg = if (amountDouble == null) "숫자만 입력해주세요." else "0보다 큰 값을 입력해주세요."
            _detailUiState.update { it.copy(amountError = errorMsg) }
            isValid = false
        }

        return isValid
    }

    private fun buildIngredientFromState(isEditMode: Boolean): Ingredient {
        val currentState = _detailUiState.value
        return Ingredient(
            id = if (isEditMode) _selectedIngredient.value?.id else null,
            name = currentState.name.trim(),
            amount = currentState.amount.toDouble(),
            unit = currentState.selectedUnit,
            expirationDate = currentState.selectedDate,
            storageLocation = currentState.selectedStorage,
            category = currentState.selectedCategory,
            emoticon = IngredientIcon.DEFAULT
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
                val message = if (isEditMode) "수정되었습니다." else "저장되었습니다."
                _operationResultEvent.emit(OperationResult.Success(message))
            } else {
                val message = if (isEditMode) "수정에 실패했습니다." else "저장에 실패했습니다."
                _operationResultEvent.emit(OperationResult.Failure(message))
            }
        }
    }

    fun onDeleteIngredient() {
        viewModelScope.launch {
            _selectedIngredient.value?.let {
                val success = delIngredientUseCase(it)
                if (success) {
                    _operationResultEvent.emit(OperationResult.Success("삭제되었습니다."))
                } else {
                    _operationResultEvent.emit(OperationResult.Failure("삭제에 실패했습니다."))
                }
            }
            _detailUiState.update { it.copy(showDeleteDialog = false) }
        }
    }
}

private fun Ingredient.toDetailUiState(): IngredientDetailUiState {
    return IngredientDetailUiState(
        name = this.name,
        amount = this.amount.toString(),
        selectedUnit = this.unit,
        selectedDate = this.expirationDate,
        selectedStorage = this.storageLocation,
        selectedCategory = this.category
    )
}