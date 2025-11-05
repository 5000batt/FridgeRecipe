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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep
import com.kjw.fridgerecipe.presentation.navigation.RECIPE_ID_DEFAULT
import com.kjw.fridgerecipe.presentation.ui.common.OperationResult
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeViewModel

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
    var servings by remember(selectedRecipe) { mutableStateOf(selectedRecipe?.servings ?: "") }
    var servingsError by remember { mutableStateOf<String?>(null) }
    var time by remember(selectedRecipe) { mutableStateOf(selectedRecipe?.time ?: "") }
    var timeError by remember { mutableStateOf<String?>(null) }
    var level by remember(selectedRecipe) { mutableStateOf(selectedRecipe?.level ?: "") }
    var levelError by remember { mutableStateOf<String?>(null) }
    val ingredientsState = remember(selectedRecipe) {
        mutableStateListOf(*(selectedRecipe?.ingredients?.toTypedArray() ?: emptyArray()))
    }
    val stepsState = remember(selectedRecipe) {
        mutableStateListOf(*(selectedRecipe?.steps?.toTypedArray() ?: emptyArray()))
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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

            OutlinedTextField(
                value = servings,
                onValueChange = { servings = it; servingsError = null },
                label = { Text("인분 (예: 2인분) *") },
                isError = servingsError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Text(
                text = servingsError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
                    .heightIn(min = 18.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = time,
                onValueChange = { time = it; timeError = null },
                label = { Text("조리 시간 (예: 30분) *") },
                isError = timeError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Text(
                text = timeError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
                    .heightIn(min = 18.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = level,
                onValueChange = { level = it; levelError = null },
                label = { Text("난이도 (예: 초급) *") },
                isError = levelError != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Text(
                text = levelError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
                    .heightIn(min = 18.dp)
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text("재료", style = MaterialTheme.typography.titleMedium)

            ingredientsState.forEachIndexed { index, ingredient ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = ingredient.name,
                        onValueChange = { newName ->
                            ingredientsState[index] = ingredient.copy(name = newName)
                        },
                        label = { Text("재료명") },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))

                    OutlinedTextField(
                        value = ingredient.quantity,
                        onValueChange = { newQty ->
                            ingredientsState[index] = ingredient.copy(quantity = newQty)
                        },
                        label = { Text("용량") },
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = { ingredientsState.removeAt(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "재료 삭제")
                    }
                }
            }

            Button(onClick = {
                ingredientsState.add(RecipeIngredient(name = "", quantity = ""))
            }) {
                Text("재료 추가")
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text("조리 순서", style = MaterialTheme.typography.titleMedium)

            stepsState.forEachIndexed { index, step ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = step.description,
                        onValueChange = { newDesc ->
                            stepsState[index] = step.copy(number = index + 1, description = newDesc)
                        },
                        label = { Text("${index + 1}. 순서") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { stepsState.removeAt(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "순서 삭제")
                    }
                }
            }

            Button(onClick = {
                stepsState.add(RecipeStep(number = stepsState.size + 1, description = ""))
            }) {
                Text("순서 추가")
            }

            Spacer(modifier = Modifier.height(32.dp))
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
                        if (title.isBlank()) {
                            titleError = "레시피 이름을 입력해주세요."
                        } else if (servings.isBlank()) {
                            servingsError = "몇 인분인지를 입력해주세요."
                        } else if (time.isBlank()) {
                            timeError = "조리 시간을 입력해주세요."
                        } else if (level.isBlank()) {
                            levelError = "조리 난이도를 입력해주세요."
                        } else {
                            val updatedRecipe = selectedRecipe!!.copy(
                                title = title.trim(),
                                servings = servings.trim(),
                                time = time.trim(),
                                level = level.trim(),
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
                    if (title.isBlank()) {
                        titleError = "레시피 이름을 입력해주세요."
                    } else if (servings.isBlank()) {
                        servingsError = "몇 인분인지를 입력해주세요."
                    } else if (time.isBlank()) {
                        timeError = "조리 시간을 입력해주세요."
                    } else if (level.isBlank()) {
                        levelError = "조리 난이도를 입력해주세요."
                    } else {
                        val newRecipe = Recipe(
                            id = null,
                            title = title.trim(),
                            servings = servings.trim(),
                            time = time.trim(),
                            level = level.trim(),
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