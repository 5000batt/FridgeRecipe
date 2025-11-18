package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import com.kjw.fridgerecipe.domain.usecase.DelIngredientUseCase
import com.kjw.fridgerecipe.domain.usecase.DelRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.GetIngredientByIdUseCase
import com.kjw.fridgerecipe.domain.usecase.GetMatchingIngredientsUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipeByIdUseCase
import com.kjw.fridgerecipe.domain.usecase.InsertRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.UpdateIngredientUseCase
import com.kjw.fridgerecipe.domain.usecase.UpdateRecipeUseCase
import com.kjw.fridgerecipe.presentation.ui.common.OperationResult
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
    val titleError: String? = null,
    val servingsState: String = "",
    val servingsError: String? = null,
    val timeState: String = "",
    val timeError: String? = null,
    val level: LevelType = LevelType.ETC,
    val categoryState: String = "상관없음",
    val utensilState: String = "상관없음",
    val ingredientsState: List<IngredientUiState> = emptyList(),
    val ingredientsError: String? = null,
    val ingredientsErrorType: ListErrorType = ListErrorType.NONE,
    val stepsState: List<StepUiState> = emptyList(),
    val stepsError: String? = null,
    val stepsErrorType: ListErrorType = ListErrorType.NONE,
    val showDeleteDialog: Boolean = false,
    val selectedRecipeTitle: String? = null
)

data class IngredientUsageState(
    val ingredientId: Long,
    val fridgeName: String,
    val recipeName: String,
    val currentAmount: Double,
    val unit: String,
    val amountToUse: String = "",
    val isError: Boolean = false
)

@HiltViewModel
class RecipeManageViewModel @Inject constructor(
    private val getSavedRecipeByIdUseCase: GetSavedRecipeByIdUseCase,
    private val insertRecipeUseCase: InsertRecipeUseCase,
    private val updateRecipeUseCase: UpdateRecipeUseCase,
    private val delRecipeUseCase: DelRecipeUseCase,
    private val ingredientRepository: IngredientRepository,
    private val getMatchingIngredientsUseCase: GetMatchingIngredientsUseCase,
    private val getIngredientByIdUseCase: GetIngredientByIdUseCase,
    private val updateIngredientUseCase: UpdateIngredientUseCase,
    private val delIngredientUseCase: DelIngredientUseCase
) : ViewModel() {

    sealed class NavigationEvent {
        data object NavigateBack : NavigationEvent()
        data object NavigateToList : NavigationEvent()
    }

    private val _operationResultEvent = MutableSharedFlow<OperationResult>()
    val operationResultEvent: SharedFlow<OperationResult> = _operationResultEvent.asSharedFlow()
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()
    private val _editUiState = MutableStateFlow(RecipeEditUiState())
    val editUiState: StateFlow<RecipeEditUiState> = _editUiState.asStateFlow()

    // RecipeDetail State
    private val _showUsageDialog = MutableStateFlow(false)
    val showUsageDialog: StateFlow<Boolean> = _showUsageDialog.asStateFlow()
    private val _usageListState = MutableStateFlow<List<IngredientUsageState>>(emptyList())
    val usageListState: StateFlow<List<IngredientUsageState>> = _usageListState.asStateFlow()
    private val _allFridgeIngredients = MutableStateFlow<List<Ingredient>>(emptyList())
    private val _filteredFridgeIngredients = MutableStateFlow<List<Ingredient>>(emptyList())
    val filteredFridgeIngredients: StateFlow<List<Ingredient>> = _filteredFridgeIngredients.asStateFlow()
    private val _showMappingSheet = MutableStateFlow<Int?>(null)
    val showMappingSheet: StateFlow<Int?> = _showMappingSheet.asStateFlow()

    fun loadRecipeById(id: Long) {
        viewModelScope.launch {
            val recipe = getSavedRecipeByIdUseCase(id)
            _selectedRecipe.value = recipe
            _editUiState.value = recipe?.toEditUiState() ?: RecipeEditUiState()
        }
    }

    fun clearSelectedRecipe() {
        _selectedRecipe.value = null
        _editUiState.value = RecipeEditUiState()
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
                val message = if (isEditMode) "수정되었습니다." else "저장되었습니다."
                _operationResultEvent.emit(OperationResult.Success(message))
                _navigationEvent.emit(NavigationEvent.NavigateBack)
            } else {
                val message = if (isEditMode) "수정에 실패했습니다." else "저장에 실패했습니다."
                _operationResultEvent.emit(OperationResult.Failure(message))
            }
        }
    }

    fun onDeleteRecipe() {
        viewModelScope.launch {
            _selectedRecipe.value?.let {
                val success = delRecipeUseCase(it)
                if (success) {
                    _operationResultEvent.emit(OperationResult.Success("삭제되었습니다."))
                    _navigationEvent.emit(NavigationEvent.NavigateToList)
                } else {
                    _operationResultEvent.emit(OperationResult.Failure("삭제에 실패했습니다."))
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
        _editUiState.update { it.copy(showDeleteDialog = true, selectedRecipeTitle = _selectedRecipe.value?.title) }
    }

    fun onDeleteDialogDismiss() {
        _editUiState.update { it.copy(showDeleteDialog = false) }
    }

    private fun checkIngredientErrors(list: List<IngredientUiState>): Pair<String?, ListErrorType> {
        return if (!list.any { it.name.isBlank() || it.quantity.isBlank() }) {
            Pair(null, ListErrorType.NONE)
        } else {
            Pair(_editUiState.value.ingredientsError, _editUiState.value.ingredientsErrorType)
        }
    }

    private fun checkStepErrors(list: List<StepUiState>): Pair<String?, ListErrorType> {
        return if (!list.any { it.description.isBlank() }) {
            Pair(null, ListErrorType.NONE)
        } else {
            Pair(_editUiState.value.stepsError, _editUiState.value.stepsErrorType)
        }
    }

    // RecipeDetailScreen Function
    fun onCookingCompleteClicked() {
        val recipe = _selectedRecipe.value ?: return
        viewModelScope.launch {
            val fridgeIngredients = ingredientRepository.getAllIngredientsSuspend()
            _allFridgeIngredients.value = fridgeIngredients
            _filteredFridgeIngredients.value = fridgeIngredients

            val matchedMap = getMatchingIngredientsUseCase(recipe)

            if (matchedMap.isEmpty()) {
                _operationResultEvent.emit(OperationResult.Failure("냉장고에서 일치하는 재료를 찾지 못했습니다."))
                return@launch
            }

            _usageListState.value = matchedMap.map { (recipeName, ingredient) ->
                IngredientUsageState(
                    ingredientId = ingredient.id!!,
                    fridgeName = ingredient.name,
                    recipeName = recipeName,
                    currentAmount = ingredient.amount,
                    unit = ingredient.unit.label
                )
            }
            _showUsageDialog.value = true
        }
    }

    fun onUsageDialogDismiss() {
        _showUsageDialog.value = false
    }

    fun onUsageAmountChanged(index: Int, newAmount: String) {
        if (newAmount.isEmpty() || newAmount.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
            _usageListState.update { list ->
                list.mapIndexed { i, item ->
                    if (i == index) item.copy(amountToUse = newAmount, isError = false) else item
                }
            }
        }
    }

    fun requestIngredientMapping(index: Int) {
        _filteredFridgeIngredients.value = _allFridgeIngredients.value
        _showMappingSheet.value = index
    }

    fun filterFridgeIngredients(query: String) {
        if (query.isBlank()) {
            _filteredFridgeIngredients.value = _allFridgeIngredients.value
        } else {
            _filteredFridgeIngredients.value = _allFridgeIngredients.value.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
    }

    fun onUsageMappingChanged(index: Int, selectedIngredientId: Long?) {
        val selectedIngredient = _allFridgeIngredients.value.find { it.id == selectedIngredientId }

        _usageListState.update { list ->
            list.mapIndexed { i, item ->
                if (i == index) {
                    item.copy(
                        ingredientId = selectedIngredient?.id,
                        fridgeName = selectedIngredient?.name,
                        unit = selectedIngredient?.unit?.label,
                        currentAmount = selectedIngredient?.amount ?: 0.0
                    )
                } else item
            }
        }
        _showMappingSheet.value = null
    }

    fun onMappingSheetDismiss() {
        _showMappingSheet.value = null
    }

    fun onConfirmUsage() {
        viewModelScope.launch {
            val usageList = _usageListState.value
            var processedCount = 0

            usageList.forEach { usage ->
                val amountUsed = usage.amountToUse.toDoubleOrNull() ?: 0.0
                if (amountUsed > 0) {
                    val originIngredient = getIngredientByIdUseCase(usage.ingredientId)

                    if (originIngredient != null) {
                        val remaining = originIngredient.amount - amountUsed

                        if (remaining <= 0) {
                            delIngredientUseCase(originIngredient)
                        } else {
                            updateIngredientUseCase(originIngredient.copy(amount = remaining))
                        }
                        processedCount++
                    }
                }
            }

            _showUsageDialog.value = false
            if (processedCount > 0) {
                _operationResultEvent.emit(OperationResult.Success("${processedCount}개 재료를 냉장고에서 차감했습니다."))
            } else {
                _operationResultEvent.emit(OperationResult.Success("차감된 재료가 없습니다."))
            }
        }
    }

    private fun validateInputs(): Boolean {
        val currentState = _editUiState.value
        var isValid = true
        var newState = currentState.copy(
            titleError = null,
            servingsError = null,
            timeError = null,
            ingredientsError = null,
            stepsError = null,
            ingredientsErrorType = ListErrorType.NONE,
            stepsErrorType = ListErrorType.NONE
        )

        if (currentState.title.isBlank()) {
            newState = newState.copy(titleError = "레시피 이름을 입력해주세요.")
            isValid = false
        }
        if (currentState.servingsState.isBlank()) {
            newState = newState.copy(servingsError = "조리 양을 입력해주세요.")
            isValid = false
        }
        if (currentState.timeState.isBlank()) {
            newState = newState.copy(timeError = "조리 시간을 입력해주세요.")
            isValid = false
        }
        if (currentState.ingredientsState.isEmpty()) {
            newState = newState.copy(ingredientsError = "재료를 추가해주세요.", ingredientsErrorType = ListErrorType.IS_EMPTY)
            isValid = false
        } else if (currentState.ingredientsState.any { it.name.isBlank() || it.quantity.isBlank()}) {
            newState = newState.copy(ingredientsError = "내용이 비어있는 재료가 있습니다.", ingredientsErrorType = ListErrorType.HAS_BLANK_ITEMS)
            isValid = false
        }
        if (currentState.stepsState.isEmpty()) {
            newState = newState.copy(stepsError = "조리 순서를 추가해주세요.", stepsErrorType = ListErrorType.IS_EMPTY)
            isValid = false
        } else if (currentState.stepsState.any {  it.description.isBlank() }) {
            newState = newState.copy(stepsError = "내용이 비어있는 조리 순서가 있습니다.", stepsErrorType = ListErrorType.HAS_BLANK_ITEMS)
            isValid = false
        }

        if (!isValid) {
            _editUiState.value = newState
        }
        return isValid
    }

    private fun buildRecipeFromState(isEditMode: Boolean): Recipe {
        val currentState = _editUiState.value
        val actualTimeInt = currentState.timeState.toIntOrNull() ?: 0
        val actualLevel = currentState.level
        val actualCategory = if (currentState.categoryState == "상관없음") null else currentState.categoryState
        val actualUtensil = if (currentState.utensilState == "상관없음") null else currentState.utensilState

        val timeFilterTag = when {
            actualTimeInt <= 15 -> "15분 이내"
            actualTimeInt <= 30 -> "30분 이내"
            actualTimeInt <= 60 -> "60분 이내"
            else -> null
        }

        val recipeId = if (isEditMode) _selectedRecipe.value?.id else null
        val ingredientsQueryTag = currentState.ingredientsState
            .filter { it.isEssential }
            .map { it.name }
            .sorted()
            .joinToString(",")

        return Recipe(
            id = recipeId,
            title = currentState.title.trim(),
            servings = "${currentState.servingsState}인분",
            time = "${currentState.timeState}분",
            level = actualLevel,
            ingredients = currentState.ingredientsState.map {
                RecipeIngredient(
                    it.name,
                    it.quantity
                )
            },
            steps = currentState.stepsState.map { RecipeStep(it.number, it.description) },
            ingredientsQuery = ingredientsQueryTag,
            timeFilter = timeFilterTag,
            levelFilter = actualLevel,
            categoryFilter = actualCategory,
            utensilFilter = actualUtensil
        )
    }
}

private fun Recipe.toEditUiState(): RecipeEditUiState {
    val essentialNames = this.ingredientsQuery
        ?.split(',')
        ?.map { it.trim() }
        ?.toSet() ?: emptySet()

    val servingsString = this.servings
    val servingsExtracted = Regex("\\d+").find(servingsString)?.value ?: ""

    val timeString = this.time
    val timeExtracted = Regex("\\d+").find(timeString)?.value ?: ""

    return RecipeEditUiState(
        title = this.title,
        servingsState = servingsExtracted,
        timeState = timeExtracted,
        level = this.level,
        categoryState = this.categoryFilter ?: "상관없음",
        utensilState = this.utensilFilter ?: "상관없음",
        ingredientsState = this.ingredients.map {
            IngredientUiState(
                name = it.name,
                quantity = it.quantity,
                isEssential = essentialNames.any { essentialName -> it.name.contains(essentialName) }
            )
        },
        stepsState = this.steps.map { StepUiState(it.number, it.description) }
    )
}