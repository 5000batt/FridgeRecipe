package com.kjw.fridgerecipe.presentation.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.presentation.ui.common.ListDisplayType
import com.kjw.fridgerecipe.presentation.ui.components.StorageSection
import com.kjw.fridgerecipe.presentation.viewmodel.IngredientViewModel
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    ingredientViewModel: IngredientViewModel = hiltViewModel(),
    recipeViewModel: RecipeViewModel = hiltViewModel(),
    onNavigateToRecipeDetail: (Long) -> Unit
) {
    DisposableEffect(Unit) {
        onDispose {
            recipeViewModel.clearSeenRecipeIds()
        }
    }

    LaunchedEffect(Unit) {
        recipeViewModel.navigationEvent.collect { event ->
            when (event) {
                is RecipeViewModel.HomeNavigationEvent.NavigateToRecipeDetail -> {
                    onNavigateToRecipeDetail(event.recipeId)
                }
                is RecipeViewModel.HomeNavigationEvent.NavigateToError -> {

                }
            }
        }
    }

    val homeIngredients by ingredientViewModel.homeScreenIngredients.collectAsState()
    val uiState by recipeViewModel.homeUiState.collectAsState()

    val timeFilterOptions = RecipeViewModel.TIME_FILTER_OPTIONS
    val levelFilterOptions = RecipeViewModel.LEVEL_FILTER_OPTIONS
    val categoryFilterOptions = RecipeViewModel.CATEGORY_FILTER_OPTIONS
    val utensilFilterOptions = RecipeViewModel.UTENSIL_FILTER_OPTIONS

    var timeMenuExpanded by remember { mutableStateOf(false) }
    var levelMenuExpanded by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var utensilMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "🥕 나의 냉장고",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            StatusIndicator(color = MaterialTheme.colorScheme.errorContainer, text = "만료")
            Spacer(modifier = Modifier.width(8.dp))
            StatusIndicator(color = MaterialTheme.colorScheme.tertiaryContainer, text = "임박")
        }

//        Spacer(modifier = Modifier.height(200.dp))

        StorageType.entries.forEach { storageType ->
            val items = homeIngredients[storageType] ?: emptyList()

            if (items.isNotEmpty()) {
                StorageSection(
                    title = storageType.label,
                    items = items,
                    displayType = ListDisplayType.ROW,
                    modifier = Modifier.padding(vertical = 8.dp),
                    selectedIngredientIds = uiState.selectedIngredientIds,
                    onIngredientClick = { ingredient ->
                        ingredient.id?.let { recipeViewModel.toggleIngredientSelection(it) }
                    }
                )
            }
        }

        if (homeIngredients.values.all { it.isEmpty() }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("냉장고가 비어있어요!", style = MaterialTheme.typography.titleMedium)
                    Text("재료 탭에서 재료를 추가해보세요.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "🍳 AI 레시피 추천",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("상세 조건 설정", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterDropdown(
                        label = "시간",
                        value = uiState.selectedTimeFilter ?: "상관없음",
                        expanded = timeMenuExpanded,
                        onExpandedChange = { timeMenuExpanded = it },
                        options = timeFilterOptions,
                        onOptionSelected = { recipeViewModel.onTimeFilterChanged(it) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterDropdown(
                        label = "난이도",
                        value = uiState.selectedLevelFilter?.label ?: "상관없음",
                        expanded = levelMenuExpanded,
                        onExpandedChange = { levelMenuExpanded = it },
                        options = levelFilterOptions.map { it?.label ?: "상관없음" },
                        onOptionSelected = { label ->
                            val level = levelFilterOptions.find { (it?.label ?: "상관없음") == label }
                            recipeViewModel.onLevelFilterChanged(level)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterDropdown(
                        label = "종류",
                        value = uiState.selectedCategoryFilter ?: "상관없음",
                        expanded = categoryMenuExpanded,
                        onExpandedChange = { categoryMenuExpanded = it },
                        options = categoryFilterOptions,
                        onOptionSelected = { recipeViewModel.onCategoryFilterChanged(it) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterDropdown(
                        label = "도구",
                        value = uiState.selectedUtensilFilter ?: "상관없음",
                        expanded = utensilMenuExpanded,
                        onExpandedChange = { utensilMenuExpanded = it },
                        options = utensilFilterOptions,
                        onOptionSelected = { recipeViewModel.onUtensilFilterChanged(it) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "선택한 재료만 사용하기",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "기본 양념을 제외한 다른 재료는 쓰지 않아요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.useOnlySelectedIngredients,
                        onCheckedChange = { recipeViewModel.onUseOnlySelectedIngredientsChanged(it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val buttonText = when {
            uiState.isRecipeLoading -> "레시피 생성 중..."
            uiState.selectedIngredientIds.isEmpty() -> "재료를 먼저 선택해주세요"
            uiState.recommendedRecipe == null -> "AI 레시피 추천 받기"
            else -> "🔄 다른 레시피 추천 받기"
        }

        Button(
            onClick = {
                val allIngredients = ingredientViewModel.allIngredients.value
                val selectedIngredients = allIngredients.filter { it.id in uiState.selectedIngredientIds }
                recipeViewModel.fetchRecommendedRecipe(selectedIngredients)
            },
            enabled = uiState.selectedIngredientIds.isNotEmpty() && !uiState.isRecipeLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            if (uiState.isRecipeLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = buttonText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
            textStyle = MaterialTheme.typography.bodyMedium,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusIndicator(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}