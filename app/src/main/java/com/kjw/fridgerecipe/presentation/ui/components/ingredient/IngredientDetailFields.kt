package com.kjw.fridgerecipe.presentation.ui.components.ingredient

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.IngredientCategoryType
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonDropdown
import java.time.LocalDate

@Composable
fun IngredientDetailFields(
    selectedCategory: IngredientCategoryType,
    selectedStorage: StorageType,
    selectedDate: LocalDate,
    onCategoryChanged: (IngredientCategoryType) -> Unit,
    onStorageChanged: (StorageType) -> Unit,
    onDatePickerShow: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Column {
        // 카테고리 선택
        CommonDropdown(
            value = stringResource(selectedCategory.labelResId),
            label = stringResource(R.string.ingredient_edit_label_category),
            options = IngredientCategoryType.entries,
            onOptionSelected = onCategoryChanged,
            itemLabel = { stringResource(it.labelResId) },
            shape = shape
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 보관 위치
        CommonDropdown(
            value = stringResource(selectedStorage.labelResId),
            label = stringResource(R.string.ingredient_edit_label_storage),
            options = StorageType.entries,
            onOptionSelected = onStorageChanged,
            itemLabel = { stringResource(it.labelResId) },
            shape = shape
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 소비 기한
        IngredientDatePicker(
            selectedDate = selectedDate,
            onClick = onDatePickerShow,
            shape = shape
        )
    }
}