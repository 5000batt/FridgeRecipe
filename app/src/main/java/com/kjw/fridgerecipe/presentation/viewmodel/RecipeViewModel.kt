package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.usecase.GetRecommendedRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    // RecipeList States
    val savedRecipes: StateFlow<List<Recipe>> = getSavedRecipesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
}
