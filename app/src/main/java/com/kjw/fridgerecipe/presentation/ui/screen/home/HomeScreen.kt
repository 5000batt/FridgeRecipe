package com.kjw.fridgerecipe.presentation.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.presentation.ui.components.ingredient.StorageSection
import com.kjw.fridgerecipe.presentation.ui.model.ListDisplayType
import com.kjw.fridgerecipe.presentation.viewmodel.FILTER_ANY
import com.kjw.fridgerecipe.presentation.viewmodel.IngredientViewModel
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeViewModel
import com.kjw.fridgerecipe.ui.theme.ExpirationContainerColor
import kotlin.math.roundToInt
import com.kjw.fridgerecipe.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    ingredientViewModel: IngredientViewModel = hiltViewModel(),
    recipeViewModel: RecipeViewModel = hiltViewModel(),
    onNavigateToRecipeDetail: (Long) -> Unit,
    onNavigateToIngredientAdd: () -> Unit
) {
    LaunchedEffect(Unit) {
        recipeViewModel.navigationEvent.collect { event ->
            when (event) {
                is RecipeViewModel.HomeNavigationEvent.NavigateToRecipeDetail -> {
                    onNavigateToRecipeDetail(event.recipeId)
                    recipeViewModel.resetHomeState()
                }

                is RecipeViewModel.HomeNavigationEvent.NavigateToError -> {

                }
            }
        }
    }

    val homeIngredients by ingredientViewModel.homeScreenIngredients.collectAsState()
    val uiState by recipeViewModel.homeUiState.collectAsState()

    val loadingTips = remember {
        listOf(
            "💡 싹 난 감자는 독성이 있으니 과감히 버리세요!",
            "💡 양파는 스타킹에 넣어 걸어두면 오래 보관할 수 있어요.",
            "💡 시들한 채소는 50도 따뜻한 물에 씻으면 싱싱해져요!",
            "💡 고기를 얼릴 때 식용유를 살짝 바르면 수분 증발을 막아줘요.",
            "💡 깐 마늘은 설탕을 뿌려 보관하면 색이 변하지 않아요.",
            "💡 먹다 남은 과자는 각설탕과 함께 보관하면 눅눅해지지 않아요."
        )
    }

    val currentTip = remember(uiState.isRecipeLoading) {
        if (uiState.isRecipeLoading) loadingTips.random() else ""
    }

    val levelFilterOptions = RecipeViewModel.LEVEL_FILTER_OPTIONS
    val categoryFilterOptions = RecipeViewModel.CATEGORY_FILTER_OPTIONS
    val utensilFilterOptions = RecipeViewModel.UTENSIL_FILTER_OPTIONS

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
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
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .clickable { onNavigateToIngredientAdd() }
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("냉장고가 비어있어요!", style = MaterialTheme.typography.titleMedium)
                        Text("터치해서 재료를 채워보세요.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "🍳 레시피 조건 설정",
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

                    /*Row(verticalAlignment = Alignment.CenterVertically) {
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

                    Spacer(modifier = Modifier.height(20.dp))*/

                    TimeSliderSection(
                        currentFilter = uiState.filterState.timeLimit,
                        onValueChange = { recipeViewModel.onTimeFilterChanged(it) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    FilterSection(
                        title = "난이도",
                        options = levelFilterOptions.map { it?.label ?: FILTER_ANY },
                        selectedOption = uiState.filterState.level?.label ?: FILTER_ANY,
                        onOptionSelected = { label ->
                            val level = levelFilterOptions.find { (it?.label ?: FILTER_ANY) == label }
                            recipeViewModel.onLevelFilterChanged(level)
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    FilterSection(
                        title = "음식 종류",
                        options = categoryFilterOptions,
                        selectedOption = uiState.filterState.category ?: FILTER_ANY,
                        onOptionSelected = { recipeViewModel.onCategoryFilterChanged(it) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

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
                                text = "기본 재료(물, 조미료 등)를 제외한 다른 재료는 쓰지 않아요.",
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
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 16.dp,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
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
            }
        }

        if (uiState.isRecipeLoading) {
            Dialog(
                onDismissRequest = { },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false,
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                RecipeLoadingScreen(tip = currentTip)
            }
        }
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

@Composable
private fun TimeSliderSection(
    currentFilter: String?,
    onValueChange: (String) -> Unit
) {
    val timeOptions = remember {
        listOf(FILTER_ANY, "15분 이내", "30분 이내", "60분 이내", "60분 초과")
    }

    val sliderValue = remember(currentFilter) {
        val targetValue = currentFilter ?: FILTER_ANY
        val index = timeOptions.indexOf(targetValue)
        if (index >= 0) index.toFloat() else 0f
    }

    val currentLabel = timeOptions.getOrNull(sliderValue.toInt()) ?: FILTER_ANY

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "조리 시간",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = currentLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                val index = newValue.roundToInt()
                val selectedOption = timeOptions.getOrNull(index) ?: FILTER_ANY
                onValueChange(selectedOption)
            },
            valueRange = 0f..(timeOptions.size - 1).toFloat(),
            steps = timeOptions.size - 2,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
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

        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                FilterChip(
                    selected = isSelected,
                    onClick = { onOptionSelected(option) },
                    label = {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        selectedBorderColor = Color.Transparent,
                        borderWidth = 1.dp
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

@Composable
private fun RecipeLoadingScreen(tip: String) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_chef))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
            .clickable(enabled = false) {}
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "AI 셰프가 레시피를 연구 중이에요...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(48.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "알고 계셨나요?",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}