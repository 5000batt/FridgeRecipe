package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.repository.SettingsRepository
import com.kjw.fridgerecipe.domain.usecase.GetRecommendedRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val FILTER_ANY = "상관없음"
data class RecipeFilterState(
    val timeLimit: String? = FILTER_ANY,
    val level: LevelType? = null,
    val category: String? = FILTER_ANY,
    val utensil: String? = FILTER_ANY,
    val useOnlySelected: Boolean = false
)

data class HomeUiState(
    val recommendedRecipe: Recipe? = null,
    val isRecipeLoading: Boolean = false,
    val selectedIngredientIds: Set<Long> = emptySet(),
    val filterState: RecipeFilterState = RecipeFilterState(),
    val showConflictDialog: Boolean = false,
    val conflictIngredients: List<String> = emptyList()
)

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val getRecommendedRecipeUseCase: GetRecommendedRecipeUseCase,
    getSavedRecipesUseCase: GetSavedRecipesUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    companion object {
        val LEVEL_FILTER_OPTIONS = listOf(null, LevelType.BEGINNER, LevelType.INTERMEDIATE, LevelType.ADVANCED)
        val CATEGORY_FILTER_OPTIONS = listOf(FILTER_ANY, "한식", "일식", "중식", "양식")
        val UTENSIL_FILTER_OPTIONS = listOf(FILTER_ANY, "에어프라이어", "전자레인지", "냄비", "후라이팬")
    }

    sealed class HomeNavigationEvent {
        data class NavigateToRecipeDetail(val recipeId: Long) : HomeNavigationEvent()
        // 추후 에러 화면 설정
        data object NavigateToError : HomeNavigationEvent()
    }

    private val _navigationEvent = MutableSharedFlow<HomeNavigationEvent>()
    val navigationEvent: SharedFlow<HomeNavigationEvent> = _navigationEvent.asSharedFlow()

    // HomeScreen States
    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()
    private val _seenRecipeIds = MutableStateFlow<Set<Long>>(emptySet())

    // RecipeListScreen States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _rawSavedRecipesFlow = getSavedRecipesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val rawSavedRecipes: StateFlow<List<Recipe>> = _rawSavedRecipesFlow

    val savedRecipes: StateFlow<List<Recipe>> =
        _searchQuery.combine(_rawSavedRecipesFlow) { query, recipes ->
            if (query.isBlank()) {
                recipes
            } else {
                recipes.filter {
                    it.title.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var currentIngredientsQuery: String = ""

    // HomeScreen Functions
    fun checkIngredientConflicts(selectedIngredients: List<Ingredient>) {
        viewModelScope.launch {
            val excludedList = settingsRepository.excludedIngredients.first().toSet()

            val conflicts = selectedIngredients
                .map { it.name }
                .filter { it in excludedList }

            if (conflicts.isNotEmpty()) {
                _homeUiState.update {
                    it.copy(
                        showConflictDialog = true,
                        conflictIngredients = conflicts
                    )
                }
            } else {
                fetchRecommendedRecipe(selectedIngredients)
            }
        }
    }

    fun fetchRecommendedRecipe(selectedIngredients: List<Ingredient>) {
        _homeUiState.update { it.copy(showConflictDialog = false, conflictIngredients = emptyList()) }

        viewModelScope.launch {
            _homeUiState.update { it.copy(isRecipeLoading = true) }
            try {
                val ingredientsQuery = selectedIngredients
                    .map { it.name }
                    .sorted()
                    .joinToString(",")

                if (ingredientsQuery != currentIngredientsQuery) {
                    _seenRecipeIds.value = emptySet()
                    currentIngredientsQuery = ingredientsQuery
                }

                val excludedList = settingsRepository.excludedIngredients.first().toList()
                val selectedIngredientNames = selectedIngredients.map { it.name }.toSet()
                val finalExcludedList = excludedList.filter { it !in selectedIngredientNames }

                val currentFilters = _homeUiState.value.filterState

                val newRecipe = getRecommendedRecipeUseCase(
                    ingredients = selectedIngredients,
                    seenIds = _seenRecipeIds.value,
                    timeFilter = currentFilters.timeLimit,
                    levelFilter = currentFilters.level,
                    categoryFilter = currentFilters.category,
                    utensilFilter = currentFilters.utensil,
                    useOnlySelected = currentFilters.useOnlySelected,
                    excludedIngredients = finalExcludedList
                )

                _homeUiState.update { it.copy(recommendedRecipe = newRecipe) }

                newRecipe?.id?.let { recipeId ->
                    _seenRecipeIds.value = _seenRecipeIds.value + recipeId
                    _navigationEvent.emit(HomeNavigationEvent.NavigateToRecipeDetail(recipeId))
                }

            } finally {
                _homeUiState.update { it.copy(isRecipeLoading = false) }
            }
        }
    }

    fun dismissConflictDialog() {
        _homeUiState.update { it.copy(showConflictDialog = false, conflictIngredients = emptyList()) }
    }

    fun toggleIngredientSelection(id: Long) {
        _homeUiState.update { currentState ->
            val currentIds = currentState.selectedIngredientIds
            val newIds = if (id in currentIds) currentIds - id else currentIds + id
            currentState.copy(selectedIngredientIds = newIds)
        }
    }

    fun onTimeFilterChanged(time: String) {
        _homeUiState.update { state ->
            state.copy(filterState = state.filterState.copy(
                timeLimit = if (time == FILTER_ANY) null else time
            ))
        }
    }

    fun onLevelFilterChanged(level: LevelType?) {
        _homeUiState.update { state ->
            state.copy(filterState = state.filterState.copy(level = level))
        }
    }

    fun onCategoryFilterChanged(category: String) {
        _homeUiState.update { state ->
            state.copy(filterState = state.filterState.copy(
                category = if (category == FILTER_ANY) null else category
            ))
        }
    }

    fun onUtensilFilterChanged(utensil: String) {
        _homeUiState.update { state ->
            state.copy(filterState = state.filterState.copy(
                utensil = if (utensil == FILTER_ANY) null else utensil
            ))
        }
    }

    fun onUseOnlySelectedIngredientsChanged(isChecked: Boolean) {
        _homeUiState.update { state ->
            state.copy(filterState = state.filterState.copy(useOnlySelected = isChecked))
        }
    }

    fun resetHomeState() {
        _homeUiState.update { state ->
            state.copy(
                selectedIngredientIds = emptySet(),
                filterState = RecipeFilterState(),
                recommendedRecipe = null
            )
        }
    }

    // RecipeListScreen Function
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }
}
