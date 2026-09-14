package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.model.RecipeWithMatch
import com.kjw.fridgerecipe.domain.usecase.CalculateRecipeMatchUseCase
import com.kjw.fridgerecipe.domain.usecase.GetIngredientsUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RecipeListViewModel
    @Inject
    constructor(
        getSavedRecipesUseCase: GetSavedRecipesUseCase,
        private val getIngredientsUseCase: GetIngredientsUseCase,
        private val calculateRecipeMatchUseCase: CalculateRecipeMatchUseCase,
    ) : ViewModel() {
        enum class SortType { LATEST, OLDEST, MATCH_RATE }

        private val _isLoading = MutableStateFlow(true)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        private val _searchQuery = MutableStateFlow("")
        val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

        private val _sortType = MutableStateFlow(SortType.LATEST)
        val sortType: StateFlow<SortType> = _sortType.asStateFlow()

        private val matchedRecipes: Flow<List<RecipeWithMatch>> =
            combine(
                getSavedRecipesUseCase().distinctUntilChanged(),
                getIngredientsUseCase().distinctUntilChanged(),
            ) { recipes, ingredients ->
                recipes.map { recipe ->
                    calculateRecipeMatchUseCase(recipe, ingredients)
                }
            }

        val recipeList: StateFlow<List<RecipeWithMatch>> =
            combine(
                matchedRecipes,
                _searchQuery,
                _sortType,
            ) { matched, query, sort ->
                val filteredList =
                    if (query.isBlank()) {
                        matched
                    } else {
                        matched.filter { it.recipe.title.contains(query, ignoreCase = true) }
                    }

                when (sort) {
                    SortType.LATEST -> filteredList.sortedByDescending { it.recipe.id }
                    SortType.OLDEST -> filteredList.sortedBy { it.recipe.id }
                    SortType.MATCH_RATE ->
                        filteredList.sortedWith(
                            compareByDescending<RecipeWithMatch> { it.matchPercentage }
                                .thenByDescending { it.recipe.id },
                        )
                }
            }.onEach {
                _isLoading.value = false
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

        fun onSearchQueryChanged(newQuery: String) {
            _searchQuery.value = newQuery
        }

        fun onSortTypeChanged(type: SortType) {
            _sortType.value = type
        }
    }
