package com.kjw.fridgerecipe.presentation.ui.screen.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.presentation.ui.components.ingredient.StorageSection
import com.kjw.fridgerecipe.presentation.ui.model.ListDisplayType
import com.kjw.fridgerecipe.presentation.viewmodel.FILTER_ANY
import com.kjw.fridgerecipe.presentation.viewmodel.IngredientViewModel
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeViewModel
import com.kjw.fridgerecipe.ui.theme.ExpirationContainerColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    ingredientViewModel: IngredientViewModel = hiltViewModel(),
    recipeViewModel: RecipeViewModel = hiltViewModel(),
    onNavigateToRecipeDetail: (Long) -> Unit
) {
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
            StatusIndicator(color = ExpirationContainerColor, text = "만료")
            Spacer(modifier = Modifier.width(8.dp))
            StatusIndicator(color = MaterialTheme.colorScheme.tertiaryContainer, text = "임박")
        }

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
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f
                    )
                ),
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
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "상세 조건 설정",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                FilterSection(
                    title = "조리 시간",
                    options = timeFilterOptions,
                    selectedOption = uiState.filterState.timeLimit ?: FILTER_ANY,
                    onOptionSelected = { recipeViewModel.onTimeFilterChanged(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                FilterSection(
                    title = "난이도",
                    options = levelFilterOptions.map { it?.label ?: FILTER_ANY },
                    selectedOption = uiState.filterState.level?.label ?: FILTER_ANY,
                    onOptionSelected = { label ->
                        val level = levelFilterOptions.find { (it?.label ?: FILTER_ANY) == label }
                        recipeViewModel.onLevelFilterChanged(level)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                FilterSection(
                    title = "음식 종류",
                    options = categoryFilterOptions,
                    selectedOption = uiState.filterState.category ?: FILTER_ANY,
                    onOptionSelected = { recipeViewModel.onCategoryFilterChanged(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                FilterSection(
                    title = "조리 도구",
                    options = utensilFilterOptions,
                    selectedOption = uiState.filterState.utensil ?: FILTER_ANY,
                    onOptionSelected = { recipeViewModel.onUtensilFilterChanged(it) }
                )

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
                        checked = uiState.filterState.useOnlySelected,
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
            else -> "다른 레시피 추천 받기"
        }

        Button(
            onClick = {
                val allIngredients = ingredientViewModel.allIngredients.value
                val selectedIngredients =
                    allIngredients.filter { it.id in uiState.selectedIngredientIds }
                recipeViewModel.checkIngredientConflicts(selectedIngredients)
            },
            enabled = uiState.selectedIngredientIds.isNotEmpty() && !uiState.isRecipeLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
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

    if (uiState.showConflictDialog) {
        val conflictNames = uiState.conflictIngredients.joinToString(", ")

        AlertDialog(
            onDismissRequest = { recipeViewModel.dismissConflictDialog() },
            title = { Text(text = "제외 재료 포함 알림") },
            text = {
                Text(
                    text = "선택하신 재료 중 '$conflictNames'은(는)\n" +
                            "설정에서 '제외할 재료'로 지정되어 있습니다.\n\n" +
                            "그래도 해당 재료를 포함하여 레시피를 추천받으시겠습니까?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val allIngredients = ingredientViewModel.allIngredients.value
                        val selectedIngredients = allIngredients.filter { it.id in uiState.selectedIngredientIds }
                        recipeViewModel.fetchRecommendedRecipe(selectedIngredients)
                    }
                ) {
                    Text("네, 포함할게요", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recipeViewModel.dismissConflictDialog() }) {
                    Text("아니요")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(options) { option ->
                val isSelected = option == selectedOption
                FilterChip(
                    selected = isSelected,
                    onClick = { onOptionSelected(option) },
                    label = { Text(option) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = Color.Transparent,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outline,
                        selectedBorderColor = Color.Transparent
                    )
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