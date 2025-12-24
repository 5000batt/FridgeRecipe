package com.kjw.fridgerecipe.presentation.ui.components.recipe.edit

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonTextField

@Composable
fun IngredientEditRow(
    isEssential: Boolean, onEssentialChange: (Boolean) -> Unit,
    name: String, onNameChange: (String) -> Unit,
    quantity: String, onQuantityChange: (String) -> Unit,
    onRemoveClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = isEssential, onCheckedChange = onEssentialChange)

        CommonTextField(
            value = name,
            onValueChange = onNameChange,
            label = stringResource(R.string.recipe_edit_label_ingredient_name),
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        CommonTextField(
            value = quantity,
            onValueChange = onQuantityChange,
            label = stringResource(R.string.recipe_edit_label_ingredient_qty),
            modifier = Modifier.weight(0.6f)
        )

        IconButton(onClick = onRemoveClick) {
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(R.string.recipe_edit_desc_delete),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}