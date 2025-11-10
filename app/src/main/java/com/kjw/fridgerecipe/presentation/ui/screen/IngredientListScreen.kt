package com.kjw.fridgerecipe.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
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
import com.kjw.fridgerecipe.domain.model.CategoryType
import com.kjw.fridgerecipe.presentation.ui.common.ListDisplayType
import com.kjw.fridgerecipe.presentation.ui.components.StorageSection
import com.kjw.fridgerecipe.presentation.viewmodel.IngredientViewModel

@Composable
fun IngredientListScreen(
    viewModel: IngredientViewModel = hiltViewModel(),
    onIngredientClick: (Long) -> Unit
) {
    val allIngredients by viewModel.ingredients.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredIngredients = remember(searchQuery, allIngredients) {
        if (searchQuery.isBlank()) {
            allIngredients
        } else {
            allIngredients.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val categorizedIngredients = remember(filteredIngredients) {
        filteredIngredients.groupBy { it.category }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 80.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("찾는 재료를 입력하세요") },
                singleLine = true,
                trailingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "검색 아이콘")
                }
            )
        }

        CategoryType.entries.forEach { categoryType ->
            val items = categorizedIngredients[categoryType] ?: emptyList()

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