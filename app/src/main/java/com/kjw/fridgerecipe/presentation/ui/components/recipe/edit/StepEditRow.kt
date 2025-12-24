package com.kjw.fridgerecipe.presentation.ui.components.recipe.edit

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonTextField

@Composable
fun StepEditRow(
    index: Int,
    description: String,
    onDescriptionChange: (String) -> Unit,
    onRemoveClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "${index + 1}.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, end = 8.dp)
        )

        CommonTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = stringResource(R.string.recipe_edit_label_step_desc),
            modifier = Modifier.weight(1f),
            singleLine = false,
            minLines = 2,
            maxLines = 4
        )

        IconButton(
            onClick = onRemoveClick,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(R.string.recipe_edit_desc_delete),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}