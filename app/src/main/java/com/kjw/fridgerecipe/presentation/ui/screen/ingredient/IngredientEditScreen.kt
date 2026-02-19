package com.kjw.fridgerecipe.presentation.ui.screen.ingredient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.IngredientCategoryType
import com.kjw.fridgerecipe.domain.model.IngredientIcon
import com.kjw.fridgerecipe.presentation.navigation.IngredientEditRoute
import com.kjw.fridgerecipe.presentation.ui.components.common.BottomActionBar
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonTopBar
import com.kjw.fridgerecipe.presentation.ui.components.common.LoadingContent
import com.kjw.fridgerecipe.presentation.ui.components.common.ConfirmDialog
import com.kjw.fridgerecipe.presentation.ui.components.common.FridgeBottomButton
import com.kjw.fridgerecipe.presentation.ui.components.ingredient.IconSelectionSection
import com.kjw.fridgerecipe.presentation.ui.components.ingredient.IngredientDetailFields
import com.kjw.fridgerecipe.presentation.ui.components.ingredient.IngredientInputFields
import com.kjw.fridgerecipe.presentation.ui.model.IngredientValidationField
import com.kjw.fridgerecipe.presentation.ui.model.OperationResult
import com.kjw.fridgerecipe.presentation.util.SnackbarType
import com.kjw.fridgerecipe.presentation.viewmodel.IngredientEditViewModel
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: IngredientEditViewModel = hiltViewModel(),
    ingredientId: Long,
    categoryId: String?,
    onShowSnackbar: (String, SnackbarType) -> Unit
) {
    val isEditMode = ingredientId != IngredientEditRoute.DEFAULT_ID
    val uiState by viewModel.editUiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    val iconListState = rememberLazyListState()

    val currentIcons = remember(uiState.selectedIconCategory) {
        if (uiState.selectedIconCategory == null) {
            IngredientIcon.entries
        } else {
            IngredientIcon.entries.filter { it.category == uiState.selectedIconCategory }
        }
    }

    var hasScrolledToInitialSelection by remember { mutableStateOf(false) }

    val nameFocusRequester = remember { FocusRequester() }
    val amountFocusRequester = remember { FocusRequester() }

    LaunchedEffect(ingredientId, isEditMode) {
        if (isEditMode) {
            viewModel.loadIngredientById(ingredientId)
        } else {
            viewModel.clearState()

            categoryId?.let { id ->
                val category = IngredientCategoryType.fromId(id)
                viewModel.onCategoryChanged(category)
                viewModel.onIconCategorySelected(category)

                val defaultIcon = IngredientIcon.entries.firstOrNull { it.category == category }
                    ?: IngredientIcon.DEFAULT
                viewModel.onIconSelected(defaultIcon)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearState()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.operationResultEvent.collect { result ->
            when (result) {
                is OperationResult.Success -> {
                    onShowSnackbar(result.message.asString(context), SnackbarType.SUCCESS)
                    onNavigateBack()
                }
                is OperationResult.Failure -> {
                    onShowSnackbar(result.message.asString(context), SnackbarType.ERROR)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.validationEvent.collect { field ->
            when (field) {
                IngredientValidationField.NAME -> nameFocusRequester.requestFocus()
                IngredientValidationField.AMOUNT -> amountFocusRequester.requestFocus()
            }
        }
    }

    LaunchedEffect(uiState.selectedIconCategory, uiState.selectedIcon) {
        if (isEditMode && !hasScrolledToInitialSelection && uiState.name.isNotEmpty()) {
            val index = currentIcons.indexOf(uiState.selectedIcon)
            if (index >= 0) {
                iconListState.scrollToItem(index)
                hasScrolledToInitialSelection = true
            }
            viewModel.onReadyToDisplay()
        }
    }

    LoadingContent(isLoading = isLoading) {
        Scaffold(
            topBar = {
                CommonTopBar(
                    title = if (isEditMode) {
                        stringResource(R.string.title_ingredient_edit)
                    } else {
                        stringResource(R.string.title_ingredient_add)
                    },
                    onNavigateBack = onNavigateBack
                )
            },
            bottomBar = {
                BottomActionBar {
                    if (isEditMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FridgeBottomButton(
                                text = stringResource(R.string.ingredient_edit_btn_delete),
                                onClick = { viewModel.onDeleteDialogShow() },
                                icon = { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.ingredient_edit_btn_delete)) },
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )

                            FridgeBottomButton(
                                text = stringResource(R.string.ingredient_edit_btn_complete),
                                onClick = { viewModel.onSaveOrUpdateIngredient(isEditMode = true) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        FridgeBottomButton(
                            text = stringResource(R.string.ingredient_edit_btn_save),
                            onClick = { viewModel.onSaveOrUpdateIngredient(isEditMode = false) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(5.dp))

                    IconSelectionSection(
                        selectedIconCategory = uiState.selectedIconCategory,
                        selectedIcon = uiState.selectedIcon,
                        currentIcons = currentIcons,
                        iconListState = iconListState,
                        onIconCategorySelected = { category ->
                            viewModel.onIconCategorySelected(category)
                            viewModel.onCategoryChanged(category)

                            val defaultIcon = IngredientIcon.entries.firstOrNull { it.category == category }
                                ?: IngredientIcon.DEFAULT
                            viewModel.onIconSelected(defaultIcon)
                        },
                        onIconSelected = { icon ->
                            viewModel.onIconSelected(icon)
                        }
                    )

                    IngredientInputFields(
                        name = uiState.name,
                        nameError = uiState.nameError?.asString(),
                        amount = uiState.amount,
                        amountError = uiState.amountError?.asString(),
                        selectedUnit = uiState.selectedUnit,
                        nameFocusRequester = nameFocusRequester,
                        amountFocusRequester = amountFocusRequester,
                        onNameChanged = { viewModel.onNameChanged(it) },
                        onAmountChanged = { viewModel.onAmountChanged(it) },
                        onUnitChanged = { viewModel.onUnitChanged(it) }
                    )

                    IngredientDetailFields(
                        selectedCategory = uiState.selectedCategory,
                        selectedStorage = uiState.selectedStorage,
                        selectedDate = uiState.selectedDate,
                        onCategoryChanged = { viewModel.onCategoryChanged(it) },
                        onStorageChanged = { viewModel.onStorageChanged(it) },
                        onDatePickerShow = { viewModel.onDatePickerDialogShow() }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }

    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.selectedDate.atStartOfDay(ZoneId.of("UTC"))
                .toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onDatePickerDialogDismiss() },
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date =
                                Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                            viewModel.onDateSelected(date)
                        }
                    }
                ) { Text(stringResource(R.string.btn_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDatePickerDialogDismiss() }) {Text(stringResource(R.string.btn_cancel)) }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    headlineContentColor = MaterialTheme.colorScheme.onSurface,
                    weekdayContentColor = MaterialTheme.colorScheme.onSurface,
                    subheadContentColor = MaterialTheme.colorScheme.onSurface,
                    yearContentColor = MaterialTheme.colorScheme.onSurface,
                    currentYearContentColor = MaterialTheme.colorScheme.primary,
                    selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary,
                    dayContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }

    if (uiState.showDeleteDialog) {
        val targetName = uiState.selectedIngredientName
            ?: stringResource(R.string.ingredient_edit_dialog_delete_msg_default)

        ConfirmDialog(
            title = stringResource(R.string.ingredient_edit_dialog_delete_title),
            message = stringResource(R.string.ingredient_edit_dialog_delete_msg, targetName),
            confirmText = stringResource(R.string.ingredient_edit_btn_delete),
            onConfirm = { viewModel.onDeleteIngredient() },
            onDismiss = { viewModel.onDeleteDialogDismiss() }
        )
    }
}