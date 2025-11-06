package com.kjw.fridgerecipe.presentation.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep
import com.kjw.fridgerecipe.presentation.navigation.RECIPE_ID_DEFAULT
import com.kjw.fridgerecipe.presentation.ui.common.OperationResult
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeViewModel

enum class ListErrorType {
    NONE,
    IS_EMPTY,
    HAS_BLANK_ITEMS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    onNavigateBack: () -> Unit,
    onNavigateToList: () -> Unit,
    viewModel: RecipeViewModel = hiltViewModel(),
    recipeId: Long
) {
    val isEditMode = recipeId != RECIPE_ID_DEFAULT
    val selectedRecipe by viewModel.selectedRecipe.collectAsState()

    LaunchedEffect(recipeId, isEditMode) {
        if (isEditMode) {
            viewModel.loadRecipeById(recipeId)
        } else {
            viewModel.clearSelectedRecipe()
        }
    }

    var title by remember(selectedRecipe) { mutableStateOf(selectedRecipe?.title ?: "") }
    var titleError by remember { mutableStateOf<String?>(null) }
    var servingsState by remember(selectedRecipe) {
        val servingString = selectedRecipe?.servings ?: ""
        val extractedNumber = Regex("\\d").find(servingString)?.value ?: ""
        mutableStateOf(extractedNumber)
    }
    var servingsError by remember { mutableStateOf<String?>(null) }
    var timeState by remember(selectedRecipe) {
        val timeString = selectedRecipe?.time ?: ""
        val extractedNumber = Regex("\\d+").find(timeString)?.value ?: ""
        mutableStateOf(extractedNumber)
    }
    var timeError by remember { mutableStateOf<String?>(null) }
    var level by remember(selectedRecipe) {
        mutableStateOf(selectedRecipe?.level ?: LevelType.ETC)
    }
    var levelMenuExpanded by remember { mutableStateOf(false) }

    val ingredientsState = remember(selectedRecipe) {
        mutableStateListOf(*(selectedRecipe?.ingredients?.toTypedArray() ?: emptyArray()))
    }
    var ingredientsError by remember { mutableStateOf<String?>(null) }
    var ingredientsErrorType by remember { mutableStateOf(ListErrorType.NONE) }
    val stepsState = remember(selectedRecipe) {
        mutableStateListOf(*(selectedRecipe?.steps?.toTypedArray() ?: emptyArray()))
    }
    var stepsError by remember { mutableStateOf<String?>(null) }
    var stepsErrorType by remember { mutableStateOf(ListErrorType.NONE) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun checkIngredientErrors() {
        val hasEmptyItem = ingredientsState.any { it.name.isBlank() || it.quantity.isBlank() }
        if (!hasEmptyItem) {
            ingredientsError = null
            ingredientsErrorType = ListErrorType.NONE
        }
    }

    fun checkStepErrors() {
        val hasEmptyItem = stepsState.any { it.description.isBlank() }
        if (!hasEmptyItem) {
            stepsError = null
            stepsErrorType = ListErrorType.NONE
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
                is RecipeViewModel.NavigationEvent.NavigateBack -> {
                    onNavigateBack()
                }
                is RecipeViewModel.NavigationEvent.NavigateToList -> {
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
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = null },
                label = { Text("레시피 제목 *") },
                isError = titleError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Text(
                text = titleError ?: "",
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
                    value = servingsState,
                    onValueChange = { newValue ->
                        if (newValue.length <= 3 && newValue.all { it.isDigit()} ) {
                            servingsState = newValue
                            servingsError = null
                        }
                    },
                    label = { Text("조리 양 *") },
                    isError = servingsError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("인분") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = timeState,
                    onValueChange = { newValue ->
                        if (newValue.length <= 3 && newValue.all { it.isDigit() }) {
                            timeState = newValue
                            timeError = null
                        }
                    },
                    label = { Text("조리 시간 *") },
                    isError = timeError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("분") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = servingsError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, top = 4.dp)
                        .heightIn(min = 18.dp)
                )
                Text(
                    text = timeError ?: "",
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
                onExpandedChange = {
                    levelMenuExpanded = !levelMenuExpanded
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = level.label,
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
                            onClick = {
                                level = levelType
                                levelMenuExpanded = false
                            }
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
                Button(onClick = {
                    ingredientsState.add(RecipeIngredient(name = "", quantity = ""))
                    if (ingredientsErrorType == ListErrorType.IS_EMPTY) {
                        ingredientsError = null
                        ingredientsErrorType = ListErrorType.NONE
                    }
                }) {
                    Text("재료 추가")
                }
            }
            Text(
                text = ingredientsError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
                    .heightIn(min = 18.dp)
            )

            ingredientsState.forEachIndexed { index, ingredient ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = ingredient.name,
                        onValueChange = { newName ->
                            ingredientsState[index] = ingredient.copy(name = newName)
                            checkIngredientErrors()
                        },
                        label = { Text("재료명") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Spacer(Modifier.width(8.dp))

                    OutlinedTextField(
                        value = ingredient.quantity,
                        onValueChange = { newQty ->
                            ingredientsState[index] = ingredient.copy(quantity = newQty)
                            checkIngredientErrors()
                        },
                        label = { Text("용량") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    IconButton(onClick = {
                        ingredientsState.removeAt(index)
                        if (ingredientsState.isEmpty()) {
                            ingredientsError = null
                            ingredientsErrorType = ListErrorType.NONE
                        } else {
                            checkIngredientErrors()
                        }
                    }) {
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
                Button(onClick = {
                    stepsState.add(RecipeStep(number = stepsState.size + 1, description = ""))
                    if (stepsErrorType == ListErrorType.IS_EMPTY) {
                        stepsError = null
                        stepsErrorType = ListErrorType.NONE
                    }
                }) {
                    Text("순서 추가")
                }
            }
            Text(
                text = stepsError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
                    .heightIn(min = 18.dp)
            )

            stepsState.forEachIndexed { index, step ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = step.description,
                        onValueChange = { newDesc ->
                            stepsState[index] = step.copy(number = index + 1, description = newDesc)
                            checkStepErrors()
                        },
                        label = { Text("${index + 1}. 순서") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        stepsState.removeAt(index)
                        if (stepsState.isEmpty()) {
                            stepsError = null
                            stepsErrorType = ListErrorType.NONE
                        } else {
                            checkStepErrors()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "순서 삭제")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        val validateInputs: () -> Boolean = {
            titleError = null
            servingsError = null
            timeError = null
            ingredientsError = null
            stepsError = null
            ingredientsErrorType = ListErrorType.NONE
            stepsErrorType = ListErrorType.NONE

            var isValid = true

            if (title.isBlank()) {
                titleError = "레시피 이름을 입력해주세요."
                isValid = false
            }
            if (servingsState.isBlank()) {
                servingsError = "조리 양을 입력해주세요."
                isValid = false
            }
            if (timeState.isBlank()) {
                timeError = "조리 시간을 입력해주세요."
                isValid = false
            }
            if (ingredientsState.isEmpty()) {
                ingredientsError = "재료를 추가해주세요."
                ingredientsErrorType = ListErrorType.IS_EMPTY
                isValid = false
            } else if (ingredientsState.any { it.name.isBlank() || it.quantity.isBlank()}) {
                ingredientsError = "내용이 비어있는 재료가 있습니다."
                ingredientsErrorType = ListErrorType.HAS_BLANK_ITEMS
                isValid = false
            }
            if (stepsState.isEmpty()) {
                stepsError = "조리 순서를 추가해주세요."
                stepsErrorType = ListErrorType.IS_EMPTY
                isValid = false
            } else if (stepsState.any {  it.description.isBlank() }) {
                stepsError = "내용이 비어있는 조리 순서가 있습니다."
                stepsErrorType = ListErrorType.HAS_BLANK_ITEMS
                isValid = false
            }

            isValid
        }

        if (isEditMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        showDeleteDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("삭제")
                }

                Button(
                    onClick = {
                        if (validateInputs()) {
                            val updatedRecipe = selectedRecipe!!.copy(
                                title = title.trim(),
                                servings = "${servingsState}인분",
                                time = "${timeState}분",
                                level = level,
                                ingredients = ingredientsState.toList(),
                                steps = stepsState.toList()
                            )

                            viewModel.updateRecipe(updatedRecipe)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("수정")
                }
            }
        } else {
            Button(
                onClick = {
                    if (validateInputs()) {
                        val newRecipe = Recipe(
                            id = null,
                            title = title.trim(),
                            servings = "${servingsState}인분",
                            time = "${timeState}분",
                            level = level,
                            ingredients = ingredientsState.toList(),
                            steps = stepsState.toList()
                        )

                        viewModel.insertRecipe(newRecipe)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("저장")
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("레시피 삭제") },
            text = { Text("정말로 '${selectedRecipe?.title}' 레시피를 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedRecipe?.let { viewModel.delRecipe(it) }
                        showDeleteDialog = false
                    }
                ) { Text("삭제", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("취소") }
            }
        )
    }
}