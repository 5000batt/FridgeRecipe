package com.kjw.fridgerecipe.presentation.ui.components.recipe.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonTextField

@Composable
fun RecipeBasicInfoForm(
    title: String,
    onTitleChange: (String) -> Unit,
    titleError: String?,
    servings: String,
    onServingsChange: (String) -> Unit,
    servingsError: String?,
    time: String,
    onTimeChange: (String) -> Unit,
    timeError: String?,
    titleFocusRequester: FocusRequester,
    servingsFocusRequester: FocusRequester,
    timeFocusRequester: FocusRequester,
) {
    Column {
        CommonTextField(
            value = title,
            onValueChange = onTitleChange,
            label = stringResource(R.string.recipe_edit_label_title),
            isError = titleError != null,
            errorMessage = titleError,
            modifier = Modifier.focusRequester(titleFocusRequester),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CommonTextField(
                value = servings,
                onValueChange = onServingsChange,
                label = stringResource(R.string.recipe_edit_label_servings),
                isError = servingsError != null,
                errorMessage = servingsError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                suffix = { Text(stringResource(R.string.recipe_edit_suffix_servings)) },
                modifier =
                    Modifier
                        .weight(1f)
                        .focusRequester(servingsFocusRequester),
            )

            CommonTextField(
                value = time,
                onValueChange = onTimeChange,
                label = stringResource(R.string.recipe_edit_label_time),
                isError = timeError != null,
                errorMessage = timeError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                suffix = { Text(stringResource(R.string.recipe_edit_suffix_time)) },
                modifier =
                    Modifier
                        .weight(1f)
                        .focusRequester(timeFocusRequester),
            )
        }
    }
}
