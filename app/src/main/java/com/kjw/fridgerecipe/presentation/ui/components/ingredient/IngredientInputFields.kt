package com.kjw.fridgerecipe.presentation.ui.components.ingredient

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.UnitType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientInputFields(
    name: String,
    nameError: String?,
    amount: String,
    amountError: String?,
    selectedUnit: UnitType,
    nameFocusRequester: FocusRequester,
    amountFocusRequester: FocusRequester,
    onNameChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onUnitChanged: (UnitType) -> Unit
) {
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline
    )
    val shape = RoundedCornerShape(12.dp)

    // 이름
    Column {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChanged,
            label = { Text(stringResource(R.string.ingredient_edit_label_name)) },
            isError = nameError != null,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(nameFocusRequester),
            singleLine = true,
            colors = textFieldColors,
            shape = shape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        if (nameError != null) {
            Text(
                text = nameError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }

    // 수량 & 단위
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChanged,
                label = { Text(stringResource(R.string.ingredient_edit_label_amount)) },
                isError = amountError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(amountFocusRequester),
                singleLine = true,
                colors = textFieldColors,
                shape = shape
            )
            Spacer(modifier = Modifier.width(8.dp))

            // 단위 선택
            var unitExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = unitExpanded,
                onExpandedChange = { unitExpanded = !unitExpanded },
                modifier = Modifier.weight(0.8f)
            ) {
                OutlinedTextField(
                    value = selectedUnit.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.ingredient_edit_label_unit)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                    modifier = Modifier.menuAnchor(),
                    colors = textFieldColors,
                    shape = shape
                )
                ExposedDropdownMenu(
                    expanded = unitExpanded,
                    onDismissRequest = { unitExpanded = false }
                ) {
                    UnitType.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.label) },
                            onClick = {
                                onUnitChanged(unit)
                                unitExpanded = false
                            }
                        )
                    }
                }
            }
        }
        if (amountError != null) {
            Text(
                text = amountError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}