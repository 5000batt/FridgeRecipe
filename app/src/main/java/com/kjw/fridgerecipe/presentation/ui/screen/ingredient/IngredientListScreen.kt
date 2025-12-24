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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.CategoryType
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonSearchBar
import com.kjw.fridgerecipe.presentation.ui.components.common.EmptyStateView
import com.kjw.fridgerecipe.presentation.ui.components.common.IngredientStatusLegend
import com.kjw.fridgerecipe.presentation.ui.components.common.LoadingContent
import com.kjw.fridgerecipe.presentation.ui.components.ingredient.StorageSection
import com.kjw.fridgerecipe.presentation.ui.model.ListDisplayType
import com.kjw.fridgerecipe.presentation.viewmodel.IngredientListViewModel

@Composable
fun IngredientListScreen(
    viewModel: IngredientListViewModel = hiltViewModel(),
    onNavigateToIngredientEdit: (Long) -> Unit
) {
    val categorizedIngredients by viewModel.categorizedIngredients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LoadingContent(isLoading = isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            CommonSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                placeholderText = stringResource(R.string.ingredient_search_placeholder),
                modifier = Modifier.fillMaxWidth()
            )

            if (categorizedIngredients.isEmpty()) {
                if (searchQuery.isNotBlank()) {
                    EmptyStateView(
                        icon = Icons.Default.Search,
                        title = stringResource(R.string.ingredient_search_empty_title, searchQuery),
                        message = stringResource(R.string.ingredient_search_empty_desc)
                    )
                }
                else {
                    EmptyStateView(
                        icon = Icons.Default.SoupKitchen,
                        title = stringResource(R.string.ingredient_empty_title),
                        message = stringResource(R.string.ingredient_empty_desc)
                    )
                }
            } else {
                IngredientStatusLegend(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                                        ingredient.id?.let { onNavigateToIngredientEdit(it) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}