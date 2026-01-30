package com.kjw.fridgerecipe.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.usecase.DelRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipeByIdUseCase
import com.kjw.fridgerecipe.domain.usecase.InsertRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.UpdateRecipeUseCase
import com.kjw.fridgerecipe.domain.util.DataResult
import com.kjw.fridgerecipe.presentation.ui.model.OperationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.model.CookingToolType
import com.kjw.fridgerecipe.domain.usecase.SaveRecipeImageUseCase
import com.kjw.fridgerecipe.presentation.mapper.RecipeUiMapper
import com.kjw.fridgerecipe.presentation.ui.model.IngredientItemUiState
import com.kjw.fridgerecipe.presentation.ui.model.ListErrorType
import com.kjw.fridgerecipe.presentation.ui.model.RecipeEditUiState
import com.kjw.fridgerecipe.presentation.ui.model.RecipeValidationField
import com.kjw.fridgerecipe.presentation.ui.model.StepItemUiState
import com.kjw.fridgerecipe.presentation.util.UiText
import com.kjw.fridgerecipe.presentation.validator.RecipeValidator

@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    private val getSavedRecipeByIdUseCase: GetSavedRecipeByIdUseCase,
    private val insertRecipeUseCase: InsertRecipeUseCase,
    private val updateRecipeUseCase: UpdateRecipeUseCase,
    private val delRecipeUseCase: DelRecipeUseCase,
    private val saveRecipeImageUseCase: SaveRecipeImageUseCase,
    private val mapper: RecipeUiMapper,
    private val validator: RecipeValidator
) : ViewModel() {

    sealed class NavigationEvent {
        data object NavigateBack : NavigationEvent()
        data object NavigateToList : NavigationEvent()
    }

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _operationResultEvent = MutableSharedFlow<OperationResult>()
    val operationResultEvent: SharedFlow<OperationResult> = _operationResultEvent.asSharedFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    private val _validationEvent = MutableSharedFlow<RecipeValidationField>()
    val validationEvent: SharedFlow<RecipeValidationField> = _validationEvent.asSharedFlow()

    private val _editUiState = MutableStateFlow(RecipeEditUiState())
    val editUiState: StateFlow<RecipeEditUiState> = _editUiState.asStateFlow()

    private var currentRecipe: Recipe? = null

    fun loadRecipeForEdit(id: Long) {
        viewModelScope.launch {
            val result = getSavedRecipeByIdUseCase(id)
            if (result is DataResult.Success) {
                val recipe = result.data
                currentRecipe = recipe
                _editUiState.value = mapper.toEditUiState(recipe)
            }
            _isLoading.value = false
        }
    }

    fun clearState() {
        currentRecipe = null
        _editUiState.value = RecipeEditUiState()
        _isLoading.value = false
    }

    fun onImageSelected(uri: Uri?) {
        uri?.let {
            viewModelScope.launch {
                val result = saveRecipeImageUseCase(it.toString())
                if (result is DataResult.Success) {
                    val savedPath = result.data
                    _editUiState.update { state -> state.copy(imageUri = savedPath) }
                } else if (result is DataResult.Error) {
                    _operationResultEvent.emit(OperationResult.Failure(result.message))
                }
            }
        }
    }

    fun onSaveOrUpdateRecipe(isEditMode: Boolean) {
        viewModelScope.launch {
            val validationResult = validator.validate(_editUiState.value)
            if (validationResult is RecipeValidator.ValidationResult.Failure) {
                updateErrorState(validationResult)
                _validationEvent.emit(validationResult.field)
                return@launch
            }

            val recipeToSave = mapper.toDomain(_editUiState.value, currentRecipe?.id)

            val result = if (isEditMode) {
                updateRecipeUseCase(recipeToSave)
            } else {
                insertRecipeUseCase(recipeToSave)
            }

            when (result) {
                is DataResult.Success -> {
                    val messageResId = if (isEditMode) R.string.msg_updated else R.string.msg_saved
                    _operationResultEvent.emit(OperationResult.Success(UiText.StringResource(messageResId)))
                    _navigationEvent.emit(NavigationEvent.NavigateBack)
                }
                is DataResult.Error -> {
                    _operationResultEvent.emit(OperationResult.Failure(result.message))
                }
                else -> Unit
            }
        }
    }

    private fun updateErrorState(failure: RecipeValidator.ValidationResult.Failure) {
        _editUiState.update { state ->
            when (failure.field) {
                RecipeValidationField.TITLE -> state.copy(titleError = failure.errorMessage)
                RecipeValidationField.SERVINGS -> state.copy(servingsError = failure.errorMessage)
                RecipeValidationField.TIME -> state.copy(timeError = failure.errorMessage)
                RecipeValidationField.INGREDIENTS -> state.copy(
                    ingredientsError = failure.errorMessage,
                    ingredientsErrorType = failure.listErrorType
                )
                RecipeValidationField.STEPS -> state.copy(
                    stepsError = failure.errorMessage,
                    stepsErrorType = failure.listErrorType
                )
            }
        }
    }

    fun onDeleteRecipe() {
        viewModelScope.launch {
            currentRecipe?.let {
                val result = delRecipeUseCase(it)
                when (result) {
                    is DataResult.Success -> {
                        _operationResultEvent.emit(OperationResult.Success(UiText.StringResource(R.string.msg_deleted)))
                        _navigationEvent.emit(NavigationEvent.NavigateToList)
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

    fun onTitleChanged(newTitle: String) {
        _editUiState.update { it.copy(title = newTitle, titleError = null) }
    }

    fun onServingsChanged(newServings: String) {
        if (newServings.length <= 3 && newServings.all { it.isDigit() }) {
            _editUiState.update { it.copy(servingsState = newServings, servingsError = null) }
        }
    }

    fun onTimeChanged(newTime: String) {
        if (newTime.length <= 3 && newTime.all { it.isDigit() }) {
            _editUiState.update { it.copy(timeState = newTime, timeError = null) }
        }
    }

    fun onLevelChanged(newLevel: LevelType) {
        _editUiState.update { it.copy(level = newLevel) }
    }

    fun onCategoryChanged(newCategory: RecipeCategoryType?) {
        _editUiState.update { it.copy(categoryState = newCategory) }
    }

    fun onCookingToolChanged(newCookingTool: CookingToolType?) {
        _editUiState.update { it.copy(cookingToolState = newCookingTool) }
    }

    fun onAddIngredient() {
        _editUiState.update { currentState ->
            val newList = currentState.ingredientsState + IngredientItemUiState("", "", false)
            val (newError, newType) = if (currentState.ingredientsErrorType == ListErrorType.IS_EMPTY) {
                Pair(null, ListErrorType.NONE)
            } else {
                Pair(currentState.ingredientsError, currentState.ingredientsErrorType)
            }
            currentState.copy(
                ingredientsState = newList,
                ingredientsError = newError,
                ingredientsErrorType = newType
            )
        }
    }

    fun onRemoveIngredient(index: Int) {
        _editUiState.update { currentState ->
            val newList = currentState.ingredientsState.toMutableList().apply { removeAt(index) }
            val (newError, newType) = checkIngredientErrors(newList)
            currentState.copy(
                ingredientsState = newList,
                ingredientsError = newError,
                ingredientsErrorType = newType
            )
        }
    }

    fun onIngredientNameChanged(index: Int, newName: String) {
        _editUiState.update { currentState ->
            val newList = currentState.ingredientsState.mapIndexed { i, item ->
                if (i == index) item.copy(name = newName) else item
            }
            val (newError, newType) = checkIngredientErrors(newList)
            currentState.copy(
                ingredientsState = newList,
                ingredientsError = newError,
                ingredientsErrorType = newType
            )
        }
    }

    fun onIngredientQuantityChanged(index: Int, newQty: String) {
        _editUiState.update { currentState ->
            val newList = currentState.ingredientsState.mapIndexed { i, item ->
                if (i == index) item.copy(quantity = newQty) else item
            }
            val (newError, newType) = checkIngredientErrors(newList)
            currentState.copy(
                ingredientsState = newList,
                ingredientsError = newError,
                ingredientsErrorType = newType
            )
        }
    }

    fun onIngredientEssentialChanged(index: Int, isChecked: Boolean) {
        _editUiState.update { currentState ->
            val newList = currentState.ingredientsState.mapIndexed { i, item ->
                if (i == index) item.copy(isEssential = isChecked) else item
            }
            currentState.copy(ingredientsState = newList)
        }
    }

    fun onAddStep() {
        _editUiState.update { currentState ->
            val newList = currentState.stepsState + StepItemUiState(
                currentState.stepsState.size + 1,
                ""
            )
            val (newError, newType) = if (currentState.stepsErrorType == ListErrorType.IS_EMPTY) {
                Pair(null, ListErrorType.NONE)
            } else {
                Pair(currentState.stepsError, currentState.stepsErrorType)
            }
            currentState.copy(
                stepsState = newList,
                stepsError = newError,
                stepsErrorType = newType
            )
        }
    }

    fun onRemoveStep(index: Int) {
        _editUiState.update { currentState ->
            val newList = currentState.stepsState.toMutableList().apply { removeAt(index) }
            val (newError, newType) = checkStepErrors(newList)
            currentState.copy(
                stepsState = newList,
                stepsError = newError,
                stepsErrorType = newType
            )
        }
    }

    fun onStepDescriptionChanged(index: Int, newDesc: String) {
        _editUiState.update { currentState ->
            val newList = currentState.stepsState.mapIndexed { i, item ->
                if (i == index) item.copy(number = i + 1, description = newDesc) else item
            }
            val (newError, newType) = checkStepErrors(newList)
            currentState.copy(
                stepsState = newList,
                stepsError = newError,
                stepsErrorType = newType
            )
        }
    }

    fun onDeleteDialogShow() {
        _editUiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onDeleteDialogDismiss() {
        _editUiState.update { it.copy(showDeleteDialog = false) }
    }

    private fun checkIngredientErrors(list: List<IngredientItemUiState>): Pair<UiText?, ListErrorType> {
        return if (!list.any { it.name.isBlank() || it.quantity.isBlank() }) {
            Pair(null, ListErrorType.NONE)
        } else {
            Pair(_editUiState.value.ingredientsError, _editUiState.value.ingredientsErrorType)
        }
    }

    private fun checkStepErrors(list: List<StepItemUiState>): Pair<UiText?, ListErrorType> {
        return if (!list.any { it.description.isBlank() }) {
            Pair(null, ListErrorType.NONE)
        } else {
            Pair(_editUiState.value.stepsError, _editUiState.value.stepsErrorType)
        }
    }
}
