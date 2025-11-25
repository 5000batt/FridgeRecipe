package com.kjw.fridgerecipe.presentation.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.domain.model.CategoryType
import com.kjw.fridgerecipe.domain.model.IngredientIcon
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.domain.model.UnitType
import com.kjw.fridgerecipe.presentation.navigation.INGREDIENT_ID_DEFAULT
import com.kjw.fridgerecipe.presentation.ui.common.OperationResult
import com.kjw.fridgerecipe.presentation.util.getIconResId
import com.kjw.fridgerecipe.presentation.viewmodel.IngredientViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: IngredientViewModel = hiltViewModel(),
    ingredientId: Long
) {
    val isEditMode = ingredientId != INGREDIENT_ID_DEFAULT
    val uiState by viewModel.editUiState.collectAsState()
    val context = LocalContext.current

    var unitExpanded by remember { mutableStateOf(false) }
    var storageExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val iconListState = rememberLazyListState()

    val currentIcons = remember(uiState.selectedIconCategory) {
        if (uiState.selectedIconCategory == null) {
            IngredientIcon.entries
        } else {
            IngredientIcon.entries.filter { it.category == uiState.selectedIconCategory }
        }
    }

    var hasScrolledToInitialSelection by remember { mutableStateOf(false) }

    LaunchedEffect(ingredientId, isEditMode) {
        if (isEditMode) {
            viewModel.loadIngredientById(ingredientId)
        } else {
            viewModel.clearSelectedIngredient()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.operationResultEvent.collect { result ->
            when (result) {
                is OperationResult.Success -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
                is OperationResult.Failure -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
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
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline
    )
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = if (isEditMode) "✏️ 재료 수정" else "📝 새 재료 추가",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )

            Column (
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "아이콘 선택",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(CategoryType.entries) { category ->
                        val isSelected = uiState.selectedIconCategory == category

                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onIconCategorySelected(category) },
                            label = { Text(category.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }

                LazyRow(
                    state = iconListState,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(currentIcons) { icon ->
                        val isSelected = uiState.selectedIcon == icon

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = LocalIndication.current
                                ) {
                                    viewModel.onIconSelected(icon)
                                }
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                .then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)) else Modifier)
                                .padding(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = getIconResId(icon)),
                                contentDescription = icon.label,
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = icon.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // 이름
            Column {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onNameChanged(it) },
                    label = { Text("재료 이름 *") },
                    isError = uiState.nameError != null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors,
                    shape = shape,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                )
                if (uiState.nameError != null) {
                    Text(
                        text = uiState.nameError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }

            // 수량 & 단위
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.amount,
                        onValueChange = { viewModel.onAmountChanged(it) },
                        label = { Text("수량 *") },
                        isError = uiState.amountError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = textFieldColors,
                        shape = shape
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = !unitExpanded },
                        modifier = Modifier.weight(0.8f)
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedUnit.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("단위") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier.menuAnchor(),
                            colors = textFieldColors,
                            shape = shape
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            UnitType.entries.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit.label) },
                                    onClick = {
                                        viewModel.onUnitChanged(unit)
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                if (uiState.amountError != null) {
                    Text(
                        text = uiState.amountError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }

            // 카테고리 선택
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.selectedCategory.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("카테고리") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = textFieldColors,
                    shape = shape
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    CategoryType.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.label) },
                            onClick = {
                                viewModel.onCategoryChanged(category)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // 보관 위치
            ExposedDropdownMenuBox(
                expanded = storageExpanded,
                onExpandedChange = { storageExpanded = !storageExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.selectedStorage.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("보관 위치") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = storageExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = textFieldColors,
                    shape = shape
                )
                ExposedDropdownMenu(
                    expanded = storageExpanded,
                    onDismissRequest = { storageExpanded = false }
                ) {
                    StorageType.entries.forEach { storage ->
                        DropdownMenuItem(
                            text = { Text(storage.label) },
                            onClick = {
                                viewModel.onStorageChanged(storage)
                                storageExpanded = false
                            }
                        )
                    }
                }
            }

            // 소비 기한
            OutlinedTextField(
                value = uiState.selectedDate.format(DateTimeFormatter.ISO_DATE),
                onValueChange = {},
                readOnly = true,
                label = { Text("소비기한 *") },
                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "날짜 선택") },
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(pass = PointerEventPass.Initial)
                            val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                            if (upEvent != null) {
                                viewModel.onDatePickerDialogShow()
                            }
                        }
                    },
                colors = textFieldColors,
                shape = shape
            )

            Spacer(modifier = Modifier.height(100.dp))
        }

        if (isEditMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.onDeleteDialogShow() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "삭제")
                }

                Button(
                    onClick = { viewModel.onSaveOrUpdateIngredient(isEditMode = true) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("수정 완료", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Button(
                onClick = { viewModel.onSaveOrUpdateIngredient(isEditMode = false) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("재료 저장", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.selectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
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
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                            viewModel.onDateSelected(date)
                        }
                    }
                ) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDatePickerDialogDismiss() }) { Text("취소") }
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
        AlertDialog(
            onDismissRequest = { viewModel.onDeleteDialogDismiss() },
            title = { Text("재료 삭제") },
            text = { Text("정말로 '${uiState.selectedIngredientName ?: "이 재료"}'를 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onDeleteIngredient() }
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onDeleteDialogDismiss() }
                ) {
                    Text("취소")
                }
            }
        )
    }
}