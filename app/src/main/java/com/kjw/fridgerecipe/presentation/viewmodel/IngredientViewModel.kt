package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.model.CategoryType
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.IngredientIcon
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.domain.model.UnitType
import com.kjw.fridgerecipe.domain.usecase.AddIngredientUseCase
import com.kjw.fridgerecipe.domain.usecase.DelIngredientUseCase
import com.kjw.fridgerecipe.domain.usecase.GetIngredientsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class IngredientViewModel @Inject constructor(
    private val addIngredientUseCase: AddIngredientUseCase,
    private val delIngredientUseCase: DelIngredientUseCase,
    getIngredientsUseCase: GetIngredientsUseCase
    ) : ViewModel() {

        init {
            viewModelScope.launch {
                addIngredientUseCase(
                    Ingredient(
                        id = 1L,
                        name = "양파",
                        amount = 3.0,
                        unit = UnitType.COUNT,
                        expirationDate = LocalDate.now().plusDays(1),
                        storageLocation = StorageType.REFRIGERATED,
                        category = CategoryType.VEGETABLE,
                        emoticon = IngredientIcon.CARROT
                    )
                )
                addIngredientUseCase(
                    Ingredient(
                        id = 2L,
                        name = "소고기",
                        amount = 500.0,
                        unit = UnitType.GRAM,
                        expirationDate = LocalDate.now().plusDays(20),
                        storageLocation = StorageType.FROZEN,
                        category = CategoryType.MEAT,
                        emoticon = IngredientIcon.DEFAULT
                    )
                )
            }
        }

    val ingredients: StateFlow<List<Ingredient>> = getIngredientsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            addIngredientUseCase(ingredient)
        }
    }

    fun delIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            delIngredientUseCase(ingredient)
        }
    }

}