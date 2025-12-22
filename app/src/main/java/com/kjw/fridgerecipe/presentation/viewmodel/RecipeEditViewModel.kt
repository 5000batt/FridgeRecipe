package com.kjw.fridgerecipe.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep
import com.kjw.fridgerecipe.domain.usecase.DelRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipeByIdUseCase
import com.kjw.fridgerecipe.domain.usecase.InsertRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.UpdateRecipeUseCase
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
import com.kjw.fridgerecipe.domain.model.RecipeSearchMetadata
import com.kjw.fridgerecipe.domain.usecase.SaveRecipeImageUseCase
import com.kjw.fridgerecipe.presentation.util.RecipeConstants.FILTER_ANY
import com.kjw.fridgerecipe.presentation.util.UiText

enum class RecipeValidationField {
    TITLE, SERVINGS, TIME, INGREDIENTS, STEPS
}

enum class ListErrorType {
    NONE,
    IS_EMPTY,
    HAS_BLANK_ITEMS
}
data class IngredientUiState(
    val name: String,
    val quantity: String,
    val isEssential: Boolean
)

data class StepUiState(
    val number: Int,
    val description: String
)

data class RecipeEditUiState(
    val title: String = "",
    val titleError: UiText? = null,
    val servingsState: String = "",
    val servingsError: UiText? = null,
    val timeState: String = "",
    val timeError: UiText? = null,
    val level: LevelType = LevelType.ETC,
    val categoryState: String = FILTER_ANY,
    val utensilState: String = FILTER_ANY,
    val ingredientsState: List<IngredientUiState> = emptyList(),
    val ingredientsError: UiText? = null,
    val ingredientsErrorType: ListErrorType = ListErrorType.NONE,
    val stepsState: List<StepUiState> = emptyList(),
    val stepsError: UiText? = null,
    val stepsErrorType: ListErrorType = ListErrorType.NONE,
    val showDeleteDialog: Boolean = false,
    val selectedRecipeTitle: String? = null,
    val imageUri: String? = null
)

@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    private val getSavedRecipeByIdUseCase: GetSavedRecipeByIdUseCase,
    private val insertRecipeUseCase: InsertRecipeUseCase,
    private val updateRecipeUseCase: UpdateRecipeUseCase,
    private val delRecipeUseCase: DelRecipeUseCase,
    private val saveRecipeImageUseCase: SaveRecipeImageUseCase
) : ViewModel() {

    sealed class NavigationEvent {
        data object NavigateBack : NavigationEvent()
        data object NavigateToList : NavigationEvent()
    }

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
            val recipe = getSavedRecipeByIdUseCase(id)
            currentRecipe = recipe
            _editUiState.value = recipe?.toEditUiState() ?: RecipeEditUiState()
        }
    }

    fun clearState() {
        currentRecipe = null
        _editUiState.value = RecipeEditUiState()
    }

    fun onImageSelected(uri: Uri?) {
        uri?.let {
            viewModelScope.launch {
                val savedPath = saveRecipeImageUseCase(it.toString())
                if (savedPath != null) {
                    _editUiState.update { state -> state.copy(imageUri = savedPath) }
                }
            }
        }
    }

    fun onSaveOrUpdateRecipe(isEditMode: Boolean) {
        viewModelScope.launch {
            if (!validateInputs()) {
                return@launch
            }

            val recipeToSave = buildRecipeFromState(isEditMode)

            val success = if (isEditMode) {
                updateRecipeUseCase(recipeToSave)
            } else {
                insertRecipeUseCase(recipeToSave)
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

    fun onDeleteRecipe() {
        viewModelScope.launch {
            currentRecipe?.let {
                val success = delRecipeUseCase(it)
                if (success) {
                    _operationResultEvent.emit(OperationResult.Success(UiText.StringResource(R.string.msg_deleted)))
                    _navigationEvent.emit(NavigationEvent.NavigateToList)
                } else {
                    _operationResultEvent.emit(OperationResult.Failure(UiText.StringResource(R.string.error_delete_failed)))
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

    fun onCategoryChanged(newCategory: String) {
        _editUiState.update { it.copy(categoryState = newCategory) }
    }

    fun onUtensilChanged(newUtensil: String) {
        _editUiState.update { it.copy(utensilState = newUtensil) }
    }

    fun onAddIngredient() {
        _editUiState.update { currentState ->
            val newList = currentState.ingredientsState + IngredientUiState("", "", false)
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
            val newList = currentState.stepsState + StepUiState(currentState.stepsState.size + 1, "")
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
        _editUiState.update { it.copy(showDeleteDialog = true, selectedRecipeTitle = _editUiState.value.title) }
    }

    fun onDeleteDialogDismiss() {
        _editUiState.update { it.copy(showDeleteDialog = false) }
    }

    private fun checkIngredientErrors(list: List<IngredientUiState>): Pair<UiText?, ListErrorType> {
        return if (!list.any { it.name.isBlank() || it.quantity.isBlank() }) {
            Pair(null, ListErrorType.NONE)
        } else {
            Pair(_editUiState.value.ingredientsError, _editUiState.value.ingredientsErrorType)
        }
    }

    private fun checkStepErrors(list: List<StepUiState>): Pair<UiText?, ListErrorType> {
        return if (!list.any { it.description.isBlank() }) {
            Pair(null, ListErrorType.NONE)
        } else {
            Pair(_editUiState.value.stepsError, _editUiState.value.stepsErrorType)
        }
    }

    private suspend fun validateInputs(): Boolean {
        val currentState = _editUiState.value

        if (currentState.title.isBlank()) {
            _editUiState.update { it.copy(titleError = UiText.StringResource(R.string.error_recipe_title_empty)) }
            _validationEvent.emit(RecipeValidationField.TITLE)
            return false
        }

        if (currentState.servingsState.isBlank()) {
            _editUiState.update { it.copy(servingsError = UiText.StringResource(R.string.error_recipe_servings_empty)) }
            _validationEvent.emit(RecipeValidationField.SERVINGS)
            return false
        }

        if (currentState.timeState.isBlank()) {
            _editUiState.update { it.copy(timeError = UiText.StringResource(R.string.error_recipe_time_empty)) }
            _validationEvent.emit(RecipeValidationField.TIME)
            return false
        }

        if (currentState.ingredientsState.isEmpty()) {
            _editUiState.update { it.copy(
                ingredientsError = UiText.StringResource(R.string.error_recipe_ingredients_empty),
                ingredientsErrorType = ListErrorType.IS_EMPTY
            ) }
            _validationEvent.emit(RecipeValidationField.INGREDIENTS)
            return false
        } else if (currentState.ingredientsState.any { it.name.isBlank() || it.quantity.isBlank() }) {
            _editUiState.update { it.copy(
                ingredientsError = UiText.StringResource(R.string.error_recipe_ingredients_blank),
                ingredientsErrorType = ListErrorType.HAS_BLANK_ITEMS
            ) }
            _validationEvent.emit(RecipeValidationField.INGREDIENTS)
            return false
        }

        if (currentState.stepsState.isEmpty()) {
            _editUiState.update { it.copy(
                stepsError = UiText.StringResource(R.string.error_recipe_steps_empty),
                stepsErrorType = ListErrorType.IS_EMPTY
            ) }
            _validationEvent.emit(RecipeValidationField.STEPS)
            return false
        } else if (currentState.stepsState.any { it.description.isBlank() }) {
            _editUiState.update { it.copy(
                stepsError = UiText.StringResource(R.string.error_recipe_steps_blank),
                stepsErrorType = ListErrorType.HAS_BLANK_ITEMS
            ) }
            _validationEvent.emit(RecipeValidationField.STEPS)
            return false
        }

        return true
    }

    private fun buildRecipeFromState(isEditMode: Boolean): Recipe {
        val currentState = _editUiState.value
        val actualTimeInt = currentState.timeState.toIntOrNull() ?: 0
        val actualLevel = currentState.level
        val actualCategory = if (currentState.categoryState == FILTER_ANY) null else currentState.categoryState
        val actualUtensil = if (currentState.utensilState == FILTER_ANY) null else currentState.utensilState

        val timeFilterTag = when {
            actualTimeInt <= 15 -> "15분 이내"
            actualTimeInt <= 30 -> "30분 이내"
            actualTimeInt <= 60 -> "60분 이내"
            else -> null
        }

        val recipeId = if (isEditMode) currentRecipe?.id else null
        val ingredientsQueryTag = currentState.ingredientsState
            .filter { it.isEssential }
            .map { it.name }
            .sorted()
            .joinToString(",")

        val metadata = RecipeSearchMetadata(
            ingredientsQuery = ingredientsQueryTag,
            timeFilter = timeFilterTag,
            levelFilter = actualLevel,
            categoryFilter = actualCategory,
            utensilFilter = actualUtensil,
            useOnlySelected = false
        )

        return Recipe(
            id = recipeId,
            title = currentState.title.trim(),
            servings = "${currentState.servingsState}인분",
            time = "${currentState.timeState}분",
            level = actualLevel,
            ingredients = currentState.ingredientsState.map {
                RecipeIngredient(
                    name = it.name,
                    quantity = it.quantity,
                    isEssential = it.isEssential
                )
            },
            steps = currentState.stepsState.map { RecipeStep(it.number, it.description) },
            searchMetadata = metadata,
            imageUri = currentState.imageUri
        )
    }
}

private fun Recipe.toEditUiState(): RecipeEditUiState {
    val servingsString = this.servings
    val servingsExtracted = Regex("\\d+").find(servingsString)?.value ?: ""

    val timeString = this.time
    val timeExtracted = Regex("\\d+").find(timeString)?.value ?: ""

    return RecipeEditUiState(
        title = this.title,
        servingsState = servingsExtracted,
        timeState = timeExtracted,
        level = this.level,
        categoryState = this.searchMetadata?.categoryFilter ?: FILTER_ANY,
        utensilState = this.searchMetadata?.utensilFilter ?: FILTER_ANY,
        ingredientsState = this.ingredients.map {
            IngredientUiState(
                name = it.name,
                quantity = it.quantity,
                isEssential = it.isEssential
            )
        },
        stepsState = this.steps.map { StepUiState(it.number, it.description) },
        imageUri = this.imageUri
    )
}