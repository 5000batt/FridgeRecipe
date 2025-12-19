package com.kjw.fridgerecipe.presentation.ui.components.ingredient

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.CategoryType
import com.kjw.fridgerecipe.domain.model.StorageType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun IngredientDetailFields(
    selectedCategory: CategoryType,
    selectedStorage: StorageType,
    selectedDate: LocalDate,
    onCategoryChanged: (CategoryType) -> Unit,
    onStorageChanged: (StorageType) -> Unit,
    onDatePickerShow: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Column {
        // 카테고리 선택
        SimpleDropdown(
            label = stringResource(R.string.ingredient_edit_label_category),
            selectedValue = selectedCategory.label,
            options = CategoryType.entries,
            onOptionSelected = onCategoryChanged,
            optionLabel = { it.label },
            shape = shape
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 보관 위치
        SimpleDropdown(
            label = stringResource(R.string.ingredient_edit_label_storage),
            selectedValue = selectedStorage.label,
            options = StorageType.entries,
            onOptionSelected = onStorageChanged,
            optionLabel = { it.label },
            shape = shape
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 소비 기한
        IngredientDatePickerField(
            selectedDate = selectedDate,
            onClick = onDatePickerShow,
            shape = shape
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SimpleDropdown(
    label: String,
    selectedValue: String,
    options: List<T>,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String,
    shape: RoundedCornerShape
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = shape
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun IngredientDatePickerField(
    selectedDate: LocalDate,
    onClick: () -> Unit,
    shape: RoundedCornerShape
) {
    OutlinedTextField(
        value = selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")),
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.ingredient_edit_label_expiration)) },
        trailingIcon = {
            Icon(
                Icons.Default.DateRange,
                contentDescription = stringResource(R.string.ingredient_edit_desc_date_select),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        onClick()
                    }
                }
            },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        shape = shape
    )
}