package com.kjw.fridgerecipe.presentation.ui.components.recipe

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeViewModel

@Composable
fun RecipeImageSelector(
    imageUri: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = stringResource(R.string.recipe_edit_image_desc),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.recipe_edit_add_photo),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeBasicInfoForm(
    title: String, onTitleChange: (String) -> Unit, titleError: String?,
    servings: String, onServingsChange: (String) -> Unit, servingsError: String?,
    time: String, onTimeChange: (String) -> Unit, timeError: String?,
    titleFocusRequester: FocusRequester,
    servingsFocusRequester: FocusRequester,
    timeFocusRequester: FocusRequester,
    transparentColors: TextFieldColors
) {
    Column {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.recipe_edit_label_title)) },
            isError = titleError != null,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(titleFocusRequester),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = transparentColors,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        if (titleError != null) ErrorText(titleError)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = servings,
                    onValueChange = onServingsChange,
                    label = { Text(stringResource(R.string.recipe_edit_label_servings)) },
                    isError = servingsError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    suffix = { Text(stringResource(R.string.recipe_edit_suffix_servings)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = transparentColors,
                    modifier = Modifier.focusRequester(servingsFocusRequester)
                )
                if (servingsError != null) ErrorText(servingsError)
            }

            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = time,
                    onValueChange = onTimeChange,
                    label = { Text(stringResource(R.string.recipe_edit_label_time)) },
                    isError = timeError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    suffix = { Text(stringResource(R.string.recipe_edit_suffix_time)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = transparentColors,
                    modifier = Modifier.focusRequester(timeFocusRequester)
                )
                if (timeError != null) ErrorText(timeError)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeMetadataForm(
    levelLabel: String, onLevelChange: (LevelType) -> Unit,
    categoryState: String, onCategoryChange: (String) -> Unit,
    utensilState: String, onUtensilChange: (String) -> Unit,
    transparentColors: TextFieldColors
) {
    Column {
        // 난이도
        DropdownField(
            value = levelLabel,
            label = stringResource(R.string.recipe_edit_label_level),
            options = LevelType.entries,
            onOptionSelected = onLevelChange,
            itemLabel = { it.label },
            colors = transparentColors
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 음식 종류
        DropdownField(
            value = categoryState,
            label = stringResource(R.string.recipe_edit_label_category),
            options = RecipeViewModel.CATEGORY_FILTER_OPTIONS,
            onOptionSelected = onCategoryChange,
            itemLabel = { it },
            colors = transparentColors
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 조리 도구
        DropdownField(
            value = utensilState,
            label = stringResource(R.string.recipe_edit_label_utensil),
            options = RecipeViewModel.UTENSIL_FILTER_OPTIONS,
            onOptionSelected = onUtensilChange,
            itemLabel = { it },
            colors = transparentColors
        )
    }
}

@Composable
fun RecipeSectionHeader(
    title: String,
    btnText: String,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text(btnText)
        }
    }
}

@Composable
fun IngredientEditRow(
    isEssential: Boolean, onEssentialChange: (Boolean) -> Unit,
    name: String, onNameChange: (String) -> Unit,
    quantity: String, onQuantityChange: (String) -> Unit,
    onRemoveClick: () -> Unit,
    transparentColors: TextFieldColors
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = isEssential, onCheckedChange = onEssentialChange)

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.recipe_edit_label_ingredient_name)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = transparentColors
        )

        Spacer(Modifier.width(8.dp))

        OutlinedTextField(
            value = quantity,
            onValueChange = onQuantityChange,
            label = { Text(stringResource(R.string.recipe_edit_label_ingredient_qty)) },
            modifier = Modifier.weight(0.6f),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = transparentColors
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

@Composable
fun StepEditRow(
    index: Int,
    description: String,
    onDescriptionChange: (String) -> Unit,
    onRemoveClick: () -> Unit,
    transparentColors: TextFieldColors
) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "${index + 1}.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, end = 8.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(R.string.recipe_edit_label_step_desc)) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = transparentColors,
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

@Composable
fun ErrorText(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownField(
    value: String,
    label: String,
    options: List<T>,
    onOptionSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    colors: TextFieldColors
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = colors
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(itemLabel(option)) },
                    onClick = { onOptionSelected(option); expanded = false }
                )
            }
        }
    }
}