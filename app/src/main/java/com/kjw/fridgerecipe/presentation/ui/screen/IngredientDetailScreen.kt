package com.kjw.fridgerecipe.presentation.ui.screen

import android.widget.Toast
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.domain.model.CategoryType
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.IngredientIcon
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.domain.model.UnitType
import com.kjw.fridgerecipe.presentation.navigation.INGREDIENT_ID_DEFAULT
import com.kjw.fridgerecipe.presentation.ui.common.OperationResult
import com.kjw.fridgerecipe.presentation.viewmodel.IngredientViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: IngredientViewModel = hiltViewModel(),
    ingredientId: Long
) {
    val isEditMode = ingredientId != INGREDIENT_ID_DEFAULT
    val selectedIngredient by viewModel.selectedIngredient.collectAsState()

    LaunchedEffect(ingredientId, isEditMode) {
        if (isEditMode) {
            viewModel.loadIngredientById(ingredientId)
        } else {
            viewModel.clearSelectedIngredient()
        }
    }

    var name by remember(selectedIngredient) { mutableStateOf(selectedIngredient?.name ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var amount by remember(selectedIngredient) { mutableStateOf(selectedIngredient?.amount?.toString() ?: "") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var selectedUnit by remember(selectedIngredient) { mutableStateOf(selectedIngredient?.unit ?: UnitType.COUNT) }
    var selectedDate by remember(selectedIngredient) { mutableStateOf(selectedIngredient?.expirationDate ?: LocalDate.now()) }
    var selectedStorage by remember(selectedIngredient) { mutableStateOf(selectedIngredient?.storageLocation ?: StorageType.REFRIGERATED) }
    var selectedCategory by remember(selectedIngredient) { mutableStateOf(selectedIngredient?.category ?: CategoryType.ETC) }

    var unitExpanded by remember { mutableStateOf(false) }
    var storageExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 이름
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; nameError = null },
            label = { Text("재료 이름 *")},
            isError = nameError != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Text(
            text = nameError ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp)
                .heightIn(min = 18.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 수량 & 단위
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    val regex = Regex("^\\d*\\.?\\d{0,2}\$")
                    if (newValue.isEmpty() || newValue.matches(regex)) {
                        amount = newValue
                        amountError = null
                    }
                },
                label = { Text("수량 *") },
                isError = amountError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            ExposedDropdownMenuBox(
                expanded = unitExpanded,
                onExpandedChange = { unitExpanded = !unitExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedUnit.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("단위") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded)},
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = unitExpanded,
                    onDismissRequest = { unitExpanded = false }
                ) {
                    UnitType.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.label) },
                            onClick = {
                                selectedUnit = unit
                                unitExpanded = false
                            }
                        )
                    }
                }
            }
        }
        Text(
            text = amountError ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp)
                .heightIn(min = 18.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 소비 기한
        OutlinedTextField(
            value = selectedDate.format(DateTimeFormatter.ISO_DATE),
            onValueChange = {},
            readOnly = true,
            label = { Text("소비기한 *")},
            trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "날짜 선택") },
            modifier = Modifier
                .fillMaxWidth()
                // https://developer.android.com/develop/ui/compose/components/datepickers#docked
                // text fields에서는 Modifier.clickable이 작동하지 않으므로 대체해서 사용(위 공식문서 참고)
                .pointerInput(selectedDate) {
                    awaitEachGesture {
                        awaitFirstDown(pass = PointerEventPass.Initial)
                        val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                        if (upEvent != null) {
                            showDatePicker = true
                        }
                    }
                }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 보관 위치
        ExposedDropdownMenuBox(
            expanded = storageExpanded,
            onExpandedChange = { storageExpanded = !storageExpanded}
        ) {
            OutlinedTextField(
                value = selectedStorage.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("보관 위치") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = storageExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = storageExpanded,
                onDismissRequest = { storageExpanded = false }
            ) {
                StorageType.entries.forEach { storage ->
                    DropdownMenuItem(
                        text = { Text(storage.label) },
                        onClick = {
                            selectedStorage = storage
                            storageExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 카테고리 선택
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded}
        ) {
            OutlinedTextField(
                value = selectedCategory.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("카테고리") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                CategoryType.entries.forEach { category ->
                    DropdownMenuItem(
                        text = { Text( category.label) },
                        onClick = {
                            selectedCategory =  category
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

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
                        val amountDouble = amount.toDoubleOrNull()
                        if (name.isBlank()) {
                            nameError = "재료 이름을 입력해주세요."
                        } else if (amountDouble == null || amountDouble <= 0) {
                            amountError = if (amountDouble == null) "숫자만 입력해주세요." else "0보다 큰 값을 입력해주세요."
                        } else {
                            val newIngredient = Ingredient(
                                id = ingredientId,
                                name = name.trim(),
                                amount = amountDouble,
                                unit = selectedUnit,
                                expirationDate = selectedDate,
                                storageLocation = selectedStorage,
                                category = selectedCategory,
                                emoticon = IngredientIcon.DEFAULT
                            )

                            viewModel.updateIngredient(newIngredient)
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
                    val amountDouble = amount.toDoubleOrNull()
                    if (name.isBlank()) {
                        nameError = "재료 이름을 입력해주세요."
                    } else if (amountDouble == null || amountDouble <= 0) {
                        amountError = if (amountDouble == null) "숫자만 입력해주세요." else "0보다 큰 값을 입력해주세요."
                    } else {
                        val newIngredient = Ingredient(
                            id = null,
                            name = name.trim(),
                            amount = amountDouble,
                            unit = selectedUnit,
                            expirationDate = selectedDate,
                            storageLocation = selectedStorage,
                            category = selectedCategory,
                            emoticon = IngredientIcon.DEFAULT
                        )

                        viewModel.addIngredient(newIngredient)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("저장")
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("재료 삭제") },
            text = { Text("정말로 '${selectedIngredient?.name ?: "이 재료"}'를 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedIngredient?.let {
                            viewModel.delIngredient(it)
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("취소")
                }
            }
        )
    }
}