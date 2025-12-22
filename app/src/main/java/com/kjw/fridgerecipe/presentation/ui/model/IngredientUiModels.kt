package com.kjw.fridgerecipe.presentation.ui.model

import com.kjw.fridgerecipe.domain.model.CategoryType
import com.kjw.fridgerecipe.domain.model.IngredientIcon
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.domain.model.UnitType
import com.kjw.fridgerecipe.presentation.util.UiText
import java.time.LocalDate

// 재료 편집 화면 상태
data class IngredientEditUiState(
    val name: String = "",
    val nameError: UiText? = null,
    val amount: String = "",
    val amountError: UiText? = null,
    val selectedUnit: UnitType = UnitType.COUNT,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedStorage: StorageType = StorageType.REFRIGERATED,
    val selectedCategory: CategoryType = CategoryType.ETC,
    val showDatePicker: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val selectedIngredientName: String? = null,
    val selectedIcon: IngredientIcon = IngredientIcon.DEFAULT,
    val selectedIconCategory: CategoryType? = null,
)

// 유효성 검사 필드 Enum
enum class IngredientValidationField {
    NAME, AMOUNT
}