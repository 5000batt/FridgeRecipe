package com.kjw.fridgerecipe.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.presentation.ui.common.ListDisplayType
import com.kjw.fridgerecipe.presentation.ui.components.RecipeCard
import com.kjw.fridgerecipe.presentation.ui.components.StorageSection
import com.kjw.fridgerecipe.presentation.viewmodel.IngredientViewModel
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    ingredientViewModel: IngredientViewModel = hiltViewModel(),
    recipeViewModel: RecipeViewModel = hiltViewModel()
) {
    val allIngredients by ingredientViewModel.ingredients.collectAsState()
    val selectedIngredientIds by recipeViewModel.selectedIngredientIds.collectAsState()
    val selectedTime by recipeViewModel.selectedTimeFilter.collectAsState()
    val selectedLevel by recipeViewModel.selectedLevelFilter.collectAsState()
    val selectedCategory by recipeViewModel.selectedCategoryFilter.collectAsState()
    val selectedUtensil by recipeViewModel.selectedUtensilFilter.collectAsState()

    val timeFilterOptions = RecipeViewModel.TIME_FILTER_OPTIONS
    val levelFilterOptions = RecipeViewModel.LEVEL_FILTER_OPTIONS
    val categoryFilterOptions = RecipeViewModel.CATEGORY_FILTER_OPTIONS
    val utensilFilterOptions = RecipeViewModel.UTENSIL_FILTER_OPTIONS

    var timeMenuExpanded by remember { mutableStateOf(false) }
    var levelMenuExpanded by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var utensilMenuExpanded by remember { mutableStateOf(false) }

    val categorizedIngredients = remember(allIngredients) {
        allIngredients.groupBy { it.storageLocation }
    }

    val recommendedRecipe by recipeViewModel.recommendedRecipe.collectAsState()
    val isRecipeLoading by recipeViewModel.isRecipeLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        StorageType.entries.forEach { storageType ->
            val items = categorizedIngredients[storageType] ?: emptyList()

            StorageSection(
                title = storageType.label,
                items = items,
                displayType = ListDisplayType.ROW,
                modifier = Modifier.padding(vertical = 8.dp),
                selectedIngredientIds = selectedIngredientIds,
                onIngredientClick = { ingredient ->
                    ingredient.id?.let { recipeViewModel.toggleIngredientSelection(it) }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "추천 조건 설정",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = timeMenuExpanded,
                onExpandedChange = { timeMenuExpanded = !timeMenuExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedTime ?: "상관없음",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("조리 시간") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = timeMenuExpanded,
                    onDismissRequest = { timeMenuExpanded = false }
                ) {
                    timeFilterOptions.forEach { time ->
                        DropdownMenuItem(
                            text = { Text(time) },
                            onClick = {
                                recipeViewModel.onTimeFilterChanged(time)
                                timeMenuExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = levelMenuExpanded,
                onExpandedChange = { levelMenuExpanded = !levelMenuExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedLevel?.label ?: "상관없음",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("난이도") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = levelMenuExpanded,
                    onDismissRequest = { levelMenuExpanded = false }
                ) {
                    levelFilterOptions.forEach { level ->
                        DropdownMenuItem(
                            text = { Text(level?.label ?: "상관없음") },
                            onClick = {
                                recipeViewModel.onLevelFilterChanged(level)
                                levelMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = categoryMenuExpanded,
                onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedCategory ?: "상관없음",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("음식 종류") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    categoryFilterOptions.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                recipeViewModel.onCategoryFilterChanged(category)
                                categoryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = utensilMenuExpanded,
                onExpandedChange = { utensilMenuExpanded = !utensilMenuExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedUtensil ?: "상관없음",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("조리 도구") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = utensilMenuExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = utensilMenuExpanded,
                    onDismissRequest = { utensilMenuExpanded = false }
                ) {
                    utensilFilterOptions.forEach { utensil ->
                        DropdownMenuItem(
                            text = { Text(utensil) },
                            onClick = {
                                recipeViewModel.onUtensilFilterChanged(utensil)
                                utensilMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "오늘의 추천 레시피",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val buttonText = when {
                isRecipeLoading -> "추천 받는 중..."
                selectedIngredientIds.isEmpty() -> "재료를 먼저 선택해주세요"
                recommendedRecipe == null -> "선택 재료로 레시피 추천 받기"
                else -> "다른 추천 받기"
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val selectedIngredients = allIngredients.filter { it.id in selectedIngredientIds }
                    recipeViewModel.fetchRecommendedRecipe(selectedIngredients)
                },
                enabled = selectedIngredientIds.isNotEmpty() && !isRecipeLoading
            ) {
                Text(buttonText)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isRecipeLoading) {
                CircularProgressIndicator()
            } else if (recommendedRecipe == null) {
                Text("추천 버튼을 눌러주세요.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    RecipeCard(recipe = recommendedRecipe!!)
                }
            }
        }
    }
}