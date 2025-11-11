package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep
import com.kjw.fridgerecipe.domain.usecase.InsertRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.DelRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.GetRecommendedRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipeByIdUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipesUseCase
import com.kjw.fridgerecipe.domain.usecase.UpdateRecipeUseCase
import com.kjw.fridgerecipe.presentation.ui.common.OperationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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
    val levelMenuExpanded: Boolean = false,
    val categoryState: String = "상관없음",
    val categoryMenuExpanded: Boolean = false,
    val utensilState: String = "상관없음",
    val utensilMenuExpanded: Boolean = false,
    val ingredientsState: List<IngredientUiState> = emptyList(),
    val ingredientsError: String? = null,
    val ingredientsErrorType: ListErrorType = ListErrorType.NONE,
    val stepsState: List<StepUiState> = emptyList(),
    val stepsError: String? = null,
    val stepsErrorType: ListErrorType = ListErrorType.NONE,
    val showDeleteDialog: Boolean = false,
    val selectedRecipeTitle: String? = null
)
@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val getRecommendedRecipeUseCase: GetRecommendedRecipeUseCase,
    getSavedRecipesUseCase: GetSavedRecipesUseCase,
    private val getSavedRecipeByIdUseCase: GetSavedRecipeByIdUseCase,
    private val insertRecipeUseCase: InsertRecipeUseCase,
    private val updateRecipeUseCase: UpdateRecipeUseCase,
    private val delRecipeUseCase: DelRecipeUseCase
) : ViewModel() {

    sealed class NavigationEvent {
        data object NavigateBack : NavigationEvent()
        data object NavigateToList : NavigationEvent()
    }

    companion object {
        val TIME_FILTER_OPTIONS = listOf("상관없음", "15분 이내", "30분 이내", "60분 이내", "60분 초과")
        val LEVEL_FILTER_OPTIONS = listOf(null) + LevelType.entries
        val CATEGORY_FILTER_OPTIONS = listOf("상관없음", "한식", "일식", "중식", "양식", "기타")
        val UTENSIL_FILTER_OPTIONS = listOf("상관없음", "에어프라이어", "전자레인지", "냄비", "후라이팬")
    }

    // HomeScreen States
    private val _recommendedRecipe = MutableStateFlow<Recipe?>(null)
    val recommendedRecipe: StateFlow<Recipe?> = _recommendedRecipe.asStateFlow()
    private val _isRecipeLoading = MutableStateFlow(false)
    val isRecipeLoading: StateFlow<Boolean> = _isRecipeLoading.asStateFlow()
    private val _selectedIngredientIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIngredientIds: StateFlow<Set<Long>> = _selectedIngredientIds.asStateFlow()
    private val _selectedTimeFilter = MutableStateFlow<String?>("상관없음")
    val selectedTimeFilter: StateFlow<String?> = _selectedTimeFilter.asStateFlow()
    private val _selectedLevelFilter = MutableStateFlow<LevelType?>(null)
    val selectedLevelFilter: StateFlow<LevelType?> = _selectedLevelFilter.asStateFlow()
    private val _selectedCategoryFilter = MutableStateFlow<String?>("상관없음")
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()
    private val _selectedUtensilFilter = MutableStateFlow<String?>("상관없음")
    val selectedUtensilFilter: StateFlow<String?> = _selectedUtensilFilter.asStateFlow()
    private val _useOnlySelectedIngredients = MutableStateFlow(false)
    val useOnlySelectedIngredients: StateFlow<Boolean> = _useOnlySelectedIngredients.asStateFlow()
    private val _seenRecipeIds = MutableStateFlow<Set<Long>>(emptySet())

    // RecipeList/Detail States
    val savedRecipes: StateFlow<List<Recipe>> = getSavedRecipesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()

    // RecipeEditScreen State
    private val _editUiState = MutableStateFlow(RecipeEditUiState())
    val editUiState: StateFlow<RecipeEditUiState> = _editUiState.asStateFlow()

    // Shared Events
    private val _operationResultEvent = MutableSharedFlow<OperationResult>()
    val operationResultEvent: SharedFlow<OperationResult> = _operationResultEvent.asSharedFlow()
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    private var currentIngredientsQuery: String = ""

    // HomeScreen Functions
    fun fetchRecommendedRecipe(selectedIngredients: List<Ingredient>) {
        viewModelScope.launch {
            _isRecipeLoading.value = true
            try {
                val ingredientsQuery = selectedIngredients
                    .map { it.name }
                    .sorted()
                    .joinToString(",")

                if (ingredientsQuery != currentIngredientsQuery) {
                    _seenRecipeIds.value = emptySet()
                    currentIngredientsQuery = ingredientsQuery
                }

                val newRecipe = getRecommendedRecipeUseCase(
                    ingredients = selectedIngredients,
                    seenIds = _seenRecipeIds.value,
                    timeFilter = _selectedTimeFilter.value,
                    levelFilter = _selectedLevelFilter.value,
                    categoryFilter = _selectedCategoryFilter.value,
                    utensilFilter = _selectedUtensilFilter.value,
                    useOnlySelected = _useOnlySelectedIngredients.value
                )

                _recommendedRecipe.value = newRecipe

                newRecipe?.id?.let {
                    _seenRecipeIds.value = _seenRecipeIds.value + it
                }

            } finally {
                _isRecipeLoading.value = false
            }
        }
    }

    fun clearSeenRecipeIds() {
        _seenRecipeIds.value = emptySet()
    }

    fun toggleIngredientSelection(id: Long) {
        val currentIds = _selectedIngredientIds.value
        if (id in currentIds) {
            _selectedIngredientIds.value = currentIds - id
        } else {
            _selectedIngredientIds.value = currentIds + id
        }
    }

    fun onTimeFilterChanged(time: String) {
        _selectedTimeFilter.value = if (time == "상관없음") null else time
    }

    fun onLevelFilterChanged(level: LevelType?) {
        _selectedLevelFilter.value = level
    }

    fun onCategoryFilterChanged(category: String) {
        _selectedCategoryFilter.value = if (category == "상관없음") null else category
    }

    fun onUtensilFilterChanged(utensil: String) {
        _selectedUtensilFilter.value = if (utensil == "상관없음") null else utensil
    }

    fun onUseOnlySelectedIngredientsChanged(isChecked: Boolean) {
        _useOnlySelectedIngredients.value = isChecked
    }

    // RecipeDetail/Edit Functions
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

    // RecipeEditScreen Event Handlers
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
        _editUiState.update { it.copy(level = newLevel, levelMenuExpanded = false) }
    }

    fun onLevelMenuExpandedChanged(isExpanded: Boolean) {
        _editUiState.update { it.copy(levelMenuExpanded = isExpanded) }
    }

    fun onCategoryChanged(newCategory: String) {
        _editUiState.update { it.copy(categoryState = newCategory, categoryMenuExpanded = false) }
    }

    fun onCategoryMenuExpandedChanged(isExpanded: Boolean) {
        _editUiState.update { it.copy(categoryMenuExpanded = isExpanded) }
    }

    fun onUtensilChanged(newUtensil: String) {
        _editUiState.update { it.copy(utensilState = newUtensil, utensilMenuExpanded = false) }
    }

    fun onUtensilMenuExpandedChanged(isExpanded: Boolean) {
        _editUiState.update { it.copy(utensilMenuExpanded = isExpanded) }
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

    // Private Helper Functions
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