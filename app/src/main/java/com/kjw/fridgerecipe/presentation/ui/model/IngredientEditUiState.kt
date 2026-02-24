package com.kjw.fridgerecipe.presentation.ui.model

import com.kjw.fridgerecipe.domain.model.IngredientCategoryType
import com.kjw.fridgerecipe.domain.model.IngredientIcon
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.domain.model.UnitType
import com.kjw.fridgerecipe.presentation.util.UiText
import java.time.LocalDate

data class IngredientEditUiState(
    val name: String = "",
    val nameError: UiText? = null,
    val amount: String = "",
    val amountError: UiText? = null,
    val selectedUnit: UnitType = UnitType.COUNT,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedStorage: StorageType = StorageType.REFRIGERATED,
    val selectedCategory: IngredientCategoryType = IngredientCategoryType.ETC,
    val showDatePicker: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val selectedIngredientName: String? = null,
    val selectedIcon: IngredientIcon = IngredientIcon.DEFAULT,
    val selectedIconCategory: IngredientCategoryType? = IngredientCategoryType.ETC,
)

enum class IngredientValidationField {
    NAME,
    AMOUNT,
}
