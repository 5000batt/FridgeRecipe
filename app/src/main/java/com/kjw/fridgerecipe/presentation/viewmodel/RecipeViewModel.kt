package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
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
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    // AI 추천 레시피 관련
    private val _recommendedRecipe = MutableStateFlow<Recipe?>(null)
    val recommendedRecipe: StateFlow<Recipe?> = _recommendedRecipe.asStateFlow()

    private val _isRecipeLoading = MutableStateFlow(false)
    val isRecipeLoading: StateFlow<Boolean> = _isRecipeLoading.asStateFlow()

    // 저장된 레시피 목록 (RecipeListScreen)
    val savedRecipes: StateFlow<List<Recipe>> = getSavedRecipesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 레시피 상세 (RecipeDetailScreen)
    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()

    // 재료 선택 (HomeScreen)
    private val _selectedIngredientIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIngredientIds: StateFlow<Set<Long>> = _selectedIngredientIds.asStateFlow()

    // 시간 선택 (HomeScreen)
    private val _selectedTimeFilter = MutableStateFlow<String?>("상관없음")
    val selectedTimeFilter: StateFlow<String?> = _selectedTimeFilter.asStateFlow()

    // 난이도 선택 (HomeScreen)
    private val _selectedLevelFilter = MutableStateFlow<LevelType?>(null)
    val selectedLevelFilter: StateFlow<LevelType?> = _selectedLevelFilter.asStateFlow()

    // 요리 카테고리 선택 (HomeScreen)
    private val _selectedCategoryFilter = MutableStateFlow<String?>("상관없음")
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    // 조리 도구 선택 (HomeScreen)
    private val _selectedUtensilFilter = MutableStateFlow<String?>("상관없음")
    val selectedUtensilFilter: StateFlow<String?> = _selectedUtensilFilter.asStateFlow()

    // 재료 스위치 상태 (HomeScreen)
    private val _useOnlySelectedIngredients = MutableStateFlow(false)
    val useOnlySelectedIngredients: StateFlow<Boolean> = _useOnlySelectedIngredients.asStateFlow()

    // 일회성 이벤트 (SharedFlow)
    private val _operationResultEvent = MutableSharedFlow<OperationResult>()
    val operationResultEvent: SharedFlow<OperationResult> = _operationResultEvent.asSharedFlow()
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    // 기타 내부 관리 변수
    private val _seenRecipeIds = MutableStateFlow<Set<Long>>(emptySet())
    private var currentIngredientsQuery: String = ""

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

    fun loadRecipeById(id: Long) {
        viewModelScope.launch {
            _selectedRecipe.value = getSavedRecipeByIdUseCase(id)
        }
    }

    fun clearSelectedRecipe() {
        _selectedRecipe.value = null
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

    fun insertRecipe(recipe: Recipe) {
        viewModelScope.launch {
            val success = insertRecipeUseCase(recipe)
            if (success) {
                _operationResultEvent.emit(OperationResult.Success("저장되었습니다."))
                _navigationEvent.emit(NavigationEvent.NavigateBack)
            } else {
                _operationResultEvent.emit(OperationResult.Failure("저장에 실패했습니다."))
            }
        }
    }

    fun updateRecipe(recipe: Recipe) {
        viewModelScope.launch {
            val success = updateRecipeUseCase(recipe)
            if (success) {
                _operationResultEvent.emit(OperationResult.Success("수정되었습니다."))
                _navigationEvent.emit(NavigationEvent.NavigateBack)
            } else {
                _operationResultEvent.emit(OperationResult.Failure("수정에 실패했습니다."))
            }
        }
    }

    fun delRecipe(recipe: Recipe) {
        viewModelScope.launch {
            val success = delRecipeUseCase(recipe)
            if (success) {
                _operationResultEvent.emit(OperationResult.Success("삭제되었습니다."))
                _navigationEvent.emit(NavigationEvent.NavigateToList)
            } else {
                _operationResultEvent.emit(OperationResult.Failure("삭제에 실패했습니다."))
            }
        }
    }
}