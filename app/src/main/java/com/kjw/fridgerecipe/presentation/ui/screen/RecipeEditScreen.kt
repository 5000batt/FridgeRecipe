package com.kjw.fridgerecipe.presentation.ui.screen

import android.widget.Toast
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.presentation.navigation.RECIPE_ID_DEFAULT
import com.kjw.fridgerecipe.presentation.ui.common.OperationResult
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeManageViewModel
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    onNavigateBack: () -> Unit,
    onNavigateToList: () -> Unit,
    viewModel: RecipeManageViewModel = hiltViewModel(),
    recipeId: Long
) {
    val isEditMode = recipeId != RECIPE_ID_DEFAULT
    val uiState by viewModel.editUiState.collectAsState()
    val context = LocalContext.current

    var levelMenuExpanded by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var utensilMenuExpanded by remember { mutableStateOf(false) }

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
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
                is OperationResult.Failure -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current
                    ) {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (uiState.imageUri != null) {
                    AsyncImage(
                        model = uiState.imageUri,
                        contentDescription = "레시피 이미지",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(48.dp))
                        Text("대표 사진 추가")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChanged(it) },
                label = { Text("레시피 제목 *") },
                isError = uiState.titleError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Text(
                text = uiState.titleError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
                    .heightIn(min = 18.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.servingsState,
                    onValueChange = { viewModel.onServingsChanged(it) },
                    label = { Text("조리 양 *") },
                    isError = uiState.servingsError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("인분") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.timeState,
                    onValueChange = { viewModel.onTimeChanged(it) },
                    label = { Text("조리 시간 *") },
                    isError = uiState.timeError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("분") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = uiState.servingsError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, top = 4.dp)
                        .heightIn(min = 18.dp)
                )
                Text(
                    text = uiState.timeError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, top = 4.dp)
                        .heightIn(min = 18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = levelMenuExpanded,
                onExpandedChange = { levelMenuExpanded = !levelMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.level.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("난이도 *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = levelMenuExpanded,
                    onDismissRequest = { levelMenuExpanded = false }
                ) {
                    LevelType.entries.forEach { levelType ->
                        DropdownMenuItem(
                            text = { Text(levelType.label) },
                            onClick = { viewModel.onLevelChanged(levelType) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = categoryMenuExpanded,
                onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.categoryState,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("음식 종류") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    RecipeViewModel.CATEGORY_FILTER_OPTIONS.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = { viewModel.onCategoryChanged(category) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = utensilMenuExpanded,
                onExpandedChange = { utensilMenuExpanded = !utensilMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.utensilState,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("주요 조리 도구") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = utensilMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = utensilMenuExpanded,
                    onDismissRequest = { utensilMenuExpanded = false }
                ) {
                    RecipeViewModel.UTENSIL_FILTER_OPTIONS.forEach { utensil ->
                        DropdownMenuItem(
                            text = { Text(utensil) },
                            onClick = { viewModel.onUtensilChanged(utensil) }
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("재료", style = MaterialTheme.typography.titleLarge)
                Button(onClick = { viewModel.onAddIngredient() }) {
                    Text("재료 추가")
                }
            }

            if (uiState.ingredientsState.isNotEmpty()) {
                Text(
                    text = "홈 화면에서 '재료로 검색' 시 사용될 '필수 재료'를 체크하세요.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }

            Text(
                text = uiState.ingredientsError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
                    .heightIn(min = 18.dp)
            )

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
                        singleLine = true
                    )

                    Spacer(Modifier.width(8.dp))

                    OutlinedTextField(
                        value = ingredient.quantity,
                        onValueChange = { newQty ->
                            viewModel.onIngredientQuantityChanged(index, newQty)
                        },
                        label = { Text("용량") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    IconButton(onClick = { viewModel.onRemoveIngredient(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "재료 삭제")
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("조리 순서", style = MaterialTheme.typography.titleLarge)
                Button(onClick = { viewModel.onAddStep() }) {
                    Text("순서 추가")
                }
            }
            Text(
                text = uiState.stepsError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
                    .heightIn(min = 18.dp)
            )

            uiState.stepsState.forEachIndexed { index, step ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = step.description,
                        onValueChange = { newDesc ->
                            viewModel.onStepDescriptionChanged(index, newDesc)
                        },
                        label = { Text("${index + 1}. 순서") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.onRemoveStep(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "순서 삭제")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (isEditMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.onDeleteDialogShow() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("삭제")
                }

                Button(
                    onClick = { viewModel.onSaveOrUpdateRecipe(isEditMode = true) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("수정")
                }
            }
        } else {
            Button(
                onClick = { viewModel.onSaveOrUpdateRecipe(isEditMode = false) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("저장")
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