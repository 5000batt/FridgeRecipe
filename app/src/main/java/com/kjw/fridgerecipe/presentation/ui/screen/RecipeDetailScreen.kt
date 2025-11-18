package com.kjw.fridgerecipe.presentation.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.presentation.ui.common.OperationResult
import com.kjw.fridgerecipe.presentation.ui.components.IngredientListItem
import com.kjw.fridgerecipe.presentation.ui.components.RecipeInfoRow
import com.kjw.fridgerecipe.presentation.ui.components.RecipeStepItem
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeManageViewModel

@Composable
fun RecipeDetailScreen(
    onNavigateToRecipeEdit: (Long) -> Unit,
    viewModel: RecipeManageViewModel = hiltViewModel(),
    recipeId: Long
) {
    val selectedRecipe by viewModel.selectedRecipe.collectAsState()
    val showUsageDialog by viewModel.showUsageDialog.collectAsState()
    val usageList by viewModel.usageListState.collectAsState()
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

    LaunchedEffect(recipeId) {
        viewModel.loadRecipeById(recipeId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSelectedRecipe()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (selectedRecipe == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val recipe = selectedRecipe!!

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                RecipeInfoRow(recipe = recipe)

                Divider(modifier = Modifier.padding(vertical = 24.dp))

                Text(
                    text = "재료",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                recipe.ingredients.forEach { ingredient ->
                    IngredientListItem(ingredient)
                }

                Divider(modifier = Modifier.padding(vertical = 24.dp))

                Text(
                    text = "조리 순서",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                recipe.steps.forEach { step ->
                    RecipeStepItem(step)
                }

                Button(
                    onClick = { viewModel.onCookingCompleteClicked() },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("요리 완료 (재료 차감)")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            Button(
                onClick = {
                    recipe.id?.let { id ->
                        onNavigateToRecipeEdit(id)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("수정")
            }
        }
    }

    if (showUsageDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onUsageDialogDismiss() },
            title = { Text("사용 재료 차감") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("냉장고에 있는 재료들입니다.\n사용하신 양만큼 입력하면 차감됩니다.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))

                    usageList.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.fridgeName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("레시피: ${item.recipeName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                Text("보유: ${item.currentAmount}${item.unit}", style = MaterialTheme.typography.bodySmall)
                            }

                            OutlinedTextField(
                                value = item.amountToUse,
                                onValueChange = { viewModel.onUsageAmountChanged(index, it) },
                                placeholder = { Text("0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(100.dp),
                                singleLine = true,
                                suffix = { Text(item.unit) }
                            )
                        }
                        Divider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onConfirmUsage() }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onUsageDialogDismiss() }) {
                    Text("취소")
                }
            }
        )
    }
}