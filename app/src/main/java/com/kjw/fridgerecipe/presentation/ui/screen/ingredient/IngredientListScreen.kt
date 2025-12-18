package com.kjw.fridgerecipe.presentation.ui.screen.ingredient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.domain.model.CategoryType
import com.kjw.fridgerecipe.presentation.ui.components.common.EmptyStateView
import com.kjw.fridgerecipe.presentation.ui.components.common.IngredientStatusLegend
import com.kjw.fridgerecipe.presentation.ui.components.ingredient.StorageSection
import com.kjw.fridgerecipe.presentation.ui.model.ListDisplayType
import com.kjw.fridgerecipe.presentation.viewmodel.IngredientViewModel

@Composable
fun IngredientListScreen(
    viewModel: IngredientViewModel = hiltViewModel(),
    onIngredientClick: (Long) -> Unit
) {
    val categorizedIngredients by viewModel.categorizedIngredients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("어떤 재료를 찾으시나요?") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "검색",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        IngredientStatusLegend(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (categorizedIngredients.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.SoupKitchen,
                title = "냉장고가 비어있어요!",
                message = "재료 탭에서 재료를 추가해보세요."
            )
        } else if (categorizedIngredients.isEmpty() && searchQuery.isNotBlank()) {
            EmptyStateView(
                icon = Icons.Default.Search,
                title = "'${searchQuery}' 검색 결과가 없습니다.",
                message = "다른 키워드로 검색해 보세요."
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                CategoryType.entries.forEach { categoryType ->
                    val items = categorizedIngredients[categoryType] ?: emptyList()

                    if (items.isNotEmpty()) {
                        item {
                            StorageSection(
                                title = categoryType.label,
                                items = items,
                                displayType = ListDisplayType.GRID,
                                selectedIngredientIds = emptySet(),
                                onIngredientClick = { ingredient ->
                                    ingredient.id?.let { onIngredientClick(it) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}