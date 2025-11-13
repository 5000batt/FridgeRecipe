package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val recommendedRecipe: Recipe? = null,
    val isRecipeLoading: Boolean = false,
    val selectedIngredientIds: Set<Long> = emptySet(),
    val selectedTimeFilter: String? = "상관없음",
    val selectedLevelFilter: LevelType? = null,
    val selectedCategoryFilter: String? = "상관없음",
    val selectedUtensilFilter: String? = "상관없음",
    val useOnlySelectedIngredients: Boolean = false
)

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val getRecommendedRecipeUseCase: GetRecommendedRecipeUseCase,
    getSavedRecipesUseCase: GetSavedRecipesUseCase
) : ViewModel() {

    companion object {
        val TIME_FILTER_OPTIONS = listOf("상관없음", "15분 이내", "30분 이내", "60분 이내", "60분 초과")
        val LEVEL_FILTER_OPTIONS = listOf(null) + LevelType.entries
        val CATEGORY_FILTER_OPTIONS = listOf("상관없음", "한식", "일식", "중식", "양식", "기타")
        val UTENSIL_FILTER_OPTIONS = listOf("상관없음", "에어프라이어", "전자레인지", "냄비", "후라이팬")
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
    fun fetchRecommendedRecipe(selectedIngredients: List<Ingredient>) {
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

                val currentState = _homeUiState.value
                val newRecipe = getRecommendedRecipeUseCase(
                    ingredients = selectedIngredients,
                    seenIds = _seenRecipeIds.value,
                    timeFilter = currentState.selectedTimeFilter,
                    levelFilter = currentState.selectedLevelFilter,
                    categoryFilter = currentState.selectedCategoryFilter,
                    utensilFilter = currentState.selectedUtensilFilter,
                    useOnlySelected = currentState.useOnlySelectedIngredients
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

    fun clearSeenRecipeIds() {
        _seenRecipeIds.value = emptySet()
    }

    fun toggleIngredientSelection(id: Long) {
        _homeUiState.update { currentState ->
            val currentIds = currentState.selectedIngredientIds
            val newIds = if (id in currentIds) currentIds - id else currentIds + id
            currentState.copy(selectedIngredientIds = newIds)
        }
    }

    fun onTimeFilterChanged(time: String) {
        _homeUiState.update {
            it.copy(selectedTimeFilter = if (time == "상관없음") null else time)
        }
    }

    fun onLevelFilterChanged(level: LevelType?) {
        _homeUiState.update { it.copy(selectedLevelFilter = level) }
    }

    fun onCategoryFilterChanged(category: String) {
        _homeUiState.update {
            it.copy(selectedCategoryFilter = if (category == "상관없음") null else category)
        }
    }

    fun onUtensilFilterChanged(utensil: String) {
        _homeUiState.update {
            it.copy(selectedUtensilFilter = if (utensil == "상관없음") null else utensil)
        }
    }

    fun onUseOnlySelectedIngredientsChanged(isChecked: Boolean) {
        _homeUiState.update { it.copy(useOnlySelectedIngredients = isChecked) }
    }

    // RecipeListScreen Function
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }
}
