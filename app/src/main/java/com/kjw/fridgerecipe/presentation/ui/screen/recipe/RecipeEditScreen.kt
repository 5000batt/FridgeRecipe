package com.kjw.fridgerecipe.presentation.ui.screen.recipe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.presentation.navigation.RECIPE_ID_DEFAULT
import com.kjw.fridgerecipe.presentation.ui.model.OperationResult
import com.kjw.fridgerecipe.presentation.util.SnackbarType
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeManageViewModel
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeValidationField
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    onNavigateBack: () -> Unit,
    onNavigateToList: () -> Unit,
    viewModel: RecipeManageViewModel = hiltViewModel(),
    recipeId: Long,
    onShowSnackbar: (String, SnackbarType) -> Unit
) {
    val isEditMode = recipeId != RECIPE_ID_DEFAULT
    val uiState by viewModel.editUiState.collectAsState()

    var levelMenuExpanded by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var utensilMenuExpanded by remember { mutableStateOf(false) }

    val titleFocusRequester = remember { FocusRequester() }
    val servingsFocusRequester = remember { FocusRequester() }
    val timeFocusRequester = remember { FocusRequester() }

    val ingredientsRequester = remember { BringIntoViewRequester() }
    val stepsRequester = remember { BringIntoViewRequester() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onImageSelected(uri) }
    )

    LaunchedEffect(recipeId, isEditMode) {
        if (isEditMode) {
            viewModel.loadRecipeById(recipeId)
        } else {
            viewModel.clearSelectedRecipe()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.operationResultEvent.collect { result ->
            when (result) {
                is OperationResult.Success -> {
                    onShowSnackbar(result.message, SnackbarType.SUCCESS)
                }

                is OperationResult.Failure -> {
                    onShowSnackbar(result.message, SnackbarType.ERROR)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is RecipeManageViewModel.NavigationEvent.NavigateBack -> {
                    onNavigateBack()
                }

                is RecipeManageViewModel.NavigationEvent.NavigateToList -> {
                    onNavigateToList()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.validationEvent.collect { field ->
            when (field) {
                RecipeValidationField.TITLE -> titleFocusRequester.requestFocus()
                RecipeValidationField.SERVINGS -> servingsFocusRequester.requestFocus()
                RecipeValidationField.TIME -> timeFocusRequester.requestFocus()
                RecipeValidationField.INGREDIENTS -> ingredientsRequester.bringIntoView()
                RecipeValidationField.STEPS -> stepsRequester.bringIntoView()
            }
        }
    }

    val transparentColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current
                    ) {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (uiState.imageUri != null) {
                        AsyncImage(
                            model = uiState.imageUri,
                            contentDescription = "레시피 이미지",
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
                                "대표 사진 추가",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChanged(it) },
                label = { Text("레시피 제목 *") },
                isError = uiState.titleError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = transparentColors,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            if (uiState.titleError != null) {
                Text(
                    text = uiState.titleError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = uiState.servingsState,
                        onValueChange = { viewModel.onServingsChanged(it) },
                        label = { Text("조리 양 *") },
                        isError = uiState.servingsError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        suffix = { Text("인분") },
                        modifier = Modifier.focusRequester(servingsFocusRequester),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = transparentColors
                    )
                    if (uiState.servingsError != null) {
                        Text(
                            text = uiState.servingsError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = uiState.timeState,
                        onValueChange = { viewModel.onTimeChanged(it) },
                        label = { Text("조리 시간 *") },
                        isError = uiState.timeError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        suffix = { Text("분") },
                        modifier = Modifier.focusRequester(timeFocusRequester),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = transparentColors
                    )
                    if (uiState.timeError != null) {
                        Text(
                            text = uiState.timeError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = levelMenuExpanded,
                onExpandedChange = { levelMenuExpanded = !levelMenuExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.level.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("난이도 *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = transparentColors
                )
                ExposedDropdownMenu(
                    expanded = levelMenuExpanded,
                    onDismissRequest = { levelMenuExpanded = false }
                ) {
                    LevelType.entries.forEach { levelType ->
                        DropdownMenuItem(
                            text = { Text(levelType.label) },
                            onClick = { viewModel.onLevelChanged(levelType); levelMenuExpanded = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = categoryMenuExpanded,
                onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.categoryState,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("음식 종류") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = transparentColors
                )
                ExposedDropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    RecipeViewModel.CATEGORY_FILTER_OPTIONS.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = { viewModel.onCategoryChanged(category); categoryMenuExpanded = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = utensilMenuExpanded,
                onExpandedChange = { utensilMenuExpanded = !utensilMenuExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.utensilState,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("주요 조리 도구") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = utensilMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = transparentColors
                )
                ExposedDropdownMenu(
                    expanded = utensilMenuExpanded,
                    onDismissRequest = { utensilMenuExpanded = false }
                ) {
                    RecipeViewModel.UTENSIL_FILTER_OPTIONS.forEach { utensil ->
                        DropdownMenuItem(
                            text = { Text(utensil) },
                            onClick = { viewModel.onUtensilChanged(utensil); utensilMenuExpanded = false }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("재료", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Button(onClick = { viewModel.onAddIngredient() }) {
                    Icon(Icons.Default.Add, contentDescription = null)

                    Spacer(Modifier.width(4.dp))

                    Text("추가")
                }
            }

            if (uiState.ingredientsState.isNotEmpty()) {
                Text(
                    text = "홈 화면에서 '재료로 검색' 시 사용될 '필수 재료'를 체크하세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (uiState.ingredientsError != null) {
                Text(
                    text = uiState.ingredientsError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 8.dp)
                        .bringIntoViewRequester(ingredientsRequester)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.ingredientsState.forEachIndexed { index, ingredient ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = ingredient.isEssential,
                            onCheckedChange = { isChecked ->
                                viewModel.onIngredientEssentialChanged(index, isChecked)
                            }
                        )

                        OutlinedTextField(
                            value = ingredient.name,
                            onValueChange = { newName ->
                                viewModel.onIngredientNameChanged(index, newName)
                            },
                            label = { Text("재료명") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = transparentColors
                        )

                        Spacer(Modifier.width(8.dp))

                        OutlinedTextField(
                            value = ingredient.quantity,
                            onValueChange = { newQty ->
                                viewModel.onIngredientQuantityChanged(index, newQty)
                            },
                            label = { Text("용량") },
                            modifier = Modifier.weight(0.6f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = transparentColors
                        )

                        IconButton(onClick = { viewModel.onRemoveIngredient(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "삭제", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("조리 순서", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Button(onClick = { viewModel.onAddStep() }) {

                    Icon(Icons.Default.Add, contentDescription = null)

                    Spacer(Modifier.width(4.dp))

                    Text("순서 추가")
                }
            }

            if (uiState.stepsError != null) {
                Text(
                    text = uiState.stepsError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 8.dp)
                        .bringIntoViewRequester(stepsRequester)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.stepsState.forEachIndexed { index, step ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, end = 8.dp)
                        )

                        OutlinedTextField(
                            value = step.description,
                            onValueChange = { viewModel.onStepDescriptionChanged(index, it) },
                            label = { Text("상세 설명") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = transparentColors,
                            minLines = 2,
                            maxLines = 4
                        )

                        IconButton(onClick = { viewModel.onRemoveStep(index) }, modifier = Modifier.padding(top = 8.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "삭제", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        if (isEditMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.onDeleteDialogShow() },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)

                    Spacer(Modifier.width(4.dp))

                    Text("삭제")
                }

                Button(
                    onClick = { viewModel.onSaveOrUpdateRecipe(isEditMode = true) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Text("수정 완료", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Button(
                onClick = { viewModel.onSaveOrUpdateRecipe(isEditMode = false) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Text("레시피 저장", fontSize = MaterialTheme.typography.titleMedium.fontSize, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDeleteDialogDismiss() },
            title = { Text("레시피 삭제") },
            text = { Text("정말로 '${uiState.selectedRecipeTitle}' 레시피를 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onDeleteRecipe() }
                ) { Text("삭제", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDeleteDialogDismiss() }) { Text("취소") }
            }
        )
    }
}