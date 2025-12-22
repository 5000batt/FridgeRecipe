package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.model.CategoryType
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.usecase.GetIngredientsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class IngredientListViewModel @Inject constructor(
    getIngredientsUseCase: GetIngredientsUseCase
) : ViewModel() {

    private val ingredientsFlow = getIngredientsUseCase()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val categorizedIngredients: StateFlow<Map<CategoryType, List<Ingredient>>> =
        combine(ingredientsFlow, _searchQuery) { ingredients, query ->
            if (query.isBlank()) {
                ingredients
            } else {
                ingredients.filter { it.name.contains(query, ignoreCase = true) }
            }
        }
        .map { filteredList ->
            filteredList.groupBy { it.category }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }
}