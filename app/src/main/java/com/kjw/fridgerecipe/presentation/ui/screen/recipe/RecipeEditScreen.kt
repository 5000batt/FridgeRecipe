package com.kjw.fridgerecipe.presentation.ui.screen.recipe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.presentation.navigation.RecipeEditRoute
import com.kjw.fridgerecipe.presentation.ui.components.common.BottomActionBar
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonTopBar
import com.kjw.fridgerecipe.presentation.ui.components.common.ConfirmDialog
import com.kjw.fridgerecipe.presentation.ui.components.common.ErrorText
import com.kjw.fridgerecipe.presentation.ui.components.common.FridgeBottomButton
import com.kjw.fridgerecipe.presentation.ui.components.common.LoadingContent
import com.kjw.fridgerecipe.presentation.ui.components.recipe.edit.IngredientEditRow
import com.kjw.fridgerecipe.presentation.ui.components.recipe.edit.RecipeBasicInfoForm
import com.kjw.fridgerecipe.presentation.ui.components.recipe.edit.RecipeImageSelector
import com.kjw.fridgerecipe.presentation.ui.components.recipe.edit.RecipeMetadataForm
import com.kjw.fridgerecipe.presentation.ui.components.recipe.edit.RecipeSectionHeader
import com.kjw.fridgerecipe.presentation.ui.components.recipe.edit.StepEditRow
import com.kjw.fridgerecipe.presentation.ui.model.OperationResult
import com.kjw.fridgerecipe.presentation.ui.model.RecipeValidationField
import com.kjw.fridgerecipe.presentation.util.SnackbarType
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRecipeList: () -> Unit,
    viewModel: RecipeEditViewModel = hiltViewModel(),
    recipeId: Long,
    onShowSnackbar: (String, SnackbarType) -> Unit,
) {
    val isEditMode = recipeId != RecipeEditRoute.DEFAULT_ID
    val uiState by viewModel.editUiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    val listState = rememberLazyListState()

    val titleFocusRequester = remember { FocusRequester() }
    val servingsFocusRequester = remember { FocusRequester() }
    val timeFocusRequester = remember { FocusRequester() }

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri -> viewModel.onImageSelected(uri) },
        )

    LaunchedEffect(recipeId, isEditMode) {
        if (isEditMode) {
            viewModel.loadRecipeForEdit(recipeId)
        } else {
            viewModel.clearState()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.operationResultEvent.collect { result ->
            when (result) {
                is OperationResult.Success -> onShowSnackbar(result.message.asString(context), SnackbarType.SUCCESS)
                is OperationResult.Failure -> onShowSnackbar(result.message.asString(context), SnackbarType.ERROR)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is RecipeEditViewModel.NavigationEvent.NavigateBack -> onNavigateBack()
                is RecipeEditViewModel.NavigationEvent.NavigateToList -> onNavigateToRecipeList()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.validationEvent.collect { field ->
            when (field) {
                RecipeValidationField.TITLE -> {
                    listState.animateScrollToItem(1)
                    titleFocusRequester.requestFocus()
                }
                RecipeValidationField.SERVINGS -> {
                    listState.animateScrollToItem(1)
                    servingsFocusRequester.requestFocus()
                }
                RecipeValidationField.TIME -> {
                    listState.animateScrollToItem(1)
                    timeFocusRequester.requestFocus()
                }
                RecipeValidationField.INGREDIENTS -> {
                    listState.animateScrollToItem(3)
                }
                RecipeValidationField.STEPS -> {
                    val stepsHeaderIndex = 4 + uiState.ingredientsState.size
                    listState.animateScrollToItem(stepsHeaderIndex)
                }
            }
        }
    }

    LoadingContent(isLoading = isLoading) {
        Scaffold(
            topBar = {
                CommonTopBar(
                    title =
                        if (isEditMode) {
                            stringResource(R.string.title_recipe_edit)
                        } else {
                            stringResource(R.string.title_recipe_add)
                        },
                    onNavigateBack = onNavigateBack,
                )
            },
            bottomBar = {
                BottomActionBar {
                    if (isEditMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            FridgeBottomButton(
                                text = stringResource(R.string.recipe_edit_btn_delete),
                                onClick = { viewModel.onDeleteDialogShow() },
                                modifier = Modifier.weight(1f),
                                icon = { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.recipe_edit_btn_delete)) },
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            )

                            FridgeBottomButton(
                                text = stringResource(R.string.recipe_edit_btn_complete),
                                onClick = { viewModel.onSaveOrUpdateRecipe(isEditMode = true) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    } else {
                        FridgeBottomButton(
                            text = stringResource(R.string.recipe_edit_btn_save),
                            onClick = { viewModel.onSaveOrUpdateRecipe(isEditMode = false) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
            ) {
                LazyColumn(
                    state = listState,
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        RecipeImageSelector(
                            imageUri = uiState.imageUri,
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))

                        RecipeBasicInfoForm(
                            title = uiState.title,
                            onTitleChange = { viewModel.onTitleChanged(it) },
                            titleError = uiState.titleError?.asString(),
                            servings = uiState.servingsState,
                            onServingsChange = { viewModel.onServingsChanged(it) },
                            servingsError = uiState.servingsError?.asString(),
                            time = uiState.timeState,
                            onTimeChange = { viewModel.onTimeChanged(it) },
                            timeError = uiState.timeError?.asString(),
                            titleFocusRequester = titleFocusRequester,
                            servingsFocusRequester = servingsFocusRequester,
                            timeFocusRequester = timeFocusRequester,
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))

                        RecipeMetadataForm(
                            selectedLevel = uiState.level,
                            onLevelChange = { viewModel.onLevelChanged(it) },
                            categoryState = uiState.categoryState,
                            onCategoryChange = { viewModel.onCategoryChanged(it) },
                            cookingToolState = uiState.cookingToolState,
                            onCookingToolChange = { viewModel.onCookingToolChanged(it) },
                        )
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        RecipeSectionHeader(
                            title = stringResource(R.string.recipe_edit_section_ingredients),
                            btnText = stringResource(R.string.recipe_edit_btn_add),
                            onAddClick = { viewModel.onAddIngredient() },
                        )

                        if (uiState.ingredientsState.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.recipe_edit_guide_ingredients),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }

                        if (uiState.ingredientsError != null) ErrorText(uiState.ingredientsError!!.asString())

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    itemsIndexed(uiState.ingredientsState) { index, ingredient ->
                        IngredientEditRow(
                            isEssential = ingredient.isEssential,
                            onEssentialChange = { viewModel.onIngredientEssentialChanged(index, it) },
                            name = ingredient.name,
                            onNameChange = { viewModel.onIngredientNameChanged(index, it) },
                            quantity = ingredient.quantity,
                            onQuantityChange = { viewModel.onIngredientQuantityChanged(index, it) },
                            onRemoveClick = { viewModel.onRemoveIngredient(index) },
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        RecipeSectionHeader(
                            title = stringResource(R.string.recipe_edit_section_steps),
                            btnText = stringResource(R.string.recipe_edit_btn_add_step),
                            onAddClick = { viewModel.onAddStep() },
                        )

                        if (uiState.stepsError != null) ErrorText(uiState.stepsError!!.asString())

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    itemsIndexed(uiState.stepsState) { index, step ->
                        StepEditRow(
                            index = index,
                            description = step.description,
                            onDescriptionChange = { viewModel.onStepDescriptionChanged(index, it) },
                            onRemoveClick = { viewModel.onRemoveStep(index) },
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }

    if (uiState.showDeleteDialog) {
        ConfirmDialog(
            title = stringResource(R.string.recipe_edit_dialog_delete_title),
            message = stringResource(R.string.recipe_edit_dialog_delete_msg, uiState.title),
            confirmText = stringResource(R.string.recipe_edit_btn_delete),
            onConfirm = { viewModel.onDeleteRecipe() },
            onDismiss = { viewModel.onDeleteDialogDismiss() },
        )
    }
}
