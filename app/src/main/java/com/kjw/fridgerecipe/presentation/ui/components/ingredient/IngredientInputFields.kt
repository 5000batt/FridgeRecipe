package com.kjw.fridgerecipe.presentation.ui.components.ingredient

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.UnitType
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonDropdown
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonTextField
import com.kjw.fridgerecipe.presentation.ui.components.common.ErrorText

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
    // 이름
    CommonTextField(
        value = name,
        onValueChange = onNameChanged,
        label = stringResource(R.string.ingredient_edit_label_name),
        isError = nameError != null,
        errorMessage = nameError,
        modifier = Modifier.focusRequester(nameFocusRequester),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
    )

    // 수량 & 단위
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            CommonTextField(
                value = amount,
                onValueChange = onAmountChanged,
                label = stringResource(R.string.ingredient_edit_label_amount),
                isError = amountError != null,
                errorMessage = null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(amountFocusRequester)
            )
            Spacer(modifier = Modifier.width(8.dp))

            // 단위 선택
            CommonDropdown(
                value = stringResource(selectedUnit.labelResId),
                label = stringResource(R.string.ingredient_edit_label_unit),
                options = UnitType.entries,
                onOptionSelected = onUnitChanged,
                itemLabel = { stringResource(it.labelResId) },
                modifier = Modifier.weight(0.8f)
            )
        }

        if (amountError != null) {
            ErrorText(message = amountError)
        }
    }
}