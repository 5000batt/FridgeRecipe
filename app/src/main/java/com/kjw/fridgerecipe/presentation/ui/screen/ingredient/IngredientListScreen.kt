package com.kjw.fridgerecipe.presentation.ui.screen.ingredient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.IngredientCategoryType
import com.kjw.fridgerecipe.presentation.navigation.IngredientEditRoute
import com.kjw.fridgerecipe.presentation.navigation.MainTab
import com.kjw.fridgerecipe.presentation.ui.components.common.BottomNavigationBar
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonFloatingActionButton
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonSearchBar
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonTopBar
import com.kjw.fridgerecipe.presentation.ui.components.common.EmptyStateView
import com.kjw.fridgerecipe.presentation.ui.components.common.FadeScrollLazyColumn
import com.kjw.fridgerecipe.presentation.ui.components.common.IngredientStatusLegend
import com.kjw.fridgerecipe.presentation.ui.components.common.LoadingContent
import com.kjw.fridgerecipe.presentation.ui.components.common.StorageSection
import com.kjw.fridgerecipe.presentation.ui.model.ListDisplayType
import com.kjw.fridgerecipe.presentation.viewmodel.IngredientListViewModel

@Composable
fun IngredientListScreen(
    viewModel: IngredientListViewModel = hiltViewModel(),
    onNavigateToIngredientEdit: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMainTab: (MainTab) -> Unit,
) {
    val categorizedIngredients by viewModel.categorizedIngredients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val listState = rememberLazyListState()

    LoadingContent(isLoading = isLoading) {
        Scaffold(
            topBar = {
                CommonTopBar(
                    title = stringResource(MainTab.INGREDIENTS.titleResId),
                    onSettingClick = onNavigateToSettings,
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    currentTab = MainTab.INGREDIENTS,
                    onTabSelected = onNavigateToMainTab,
                )
            },
            floatingActionButton = {
                CommonFloatingActionButton(
                    text = stringResource(R.string.fab_add_ingredient),
                    icon = Icons.Filled.Add,
                    state = listState,
                    onClick = { onNavigateToIngredientEdit(IngredientEditRoute.DEFAULT_ID) },
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
            ) {
                CommonSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    placeholderText = stringResource(R.string.ingredient_search_placeholder),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (categorizedIngredients.isEmpty()) {
                    if (searchQuery.isNotBlank()) {
                        EmptyStateView(
                            icon = Icons.Default.Search,
                            title = stringResource(R.string.ingredient_search_empty_title, searchQuery),
                            message = stringResource(R.string.ingredient_search_empty_desc),
                        )
                    } else {
                        EmptyStateView(
                            icon = Icons.Default.SoupKitchen,
                            title = stringResource(R.string.ingredient_empty_title),
                            message = stringResource(R.string.ingredient_empty_desc),
                        )
                    }
                } else {
                    IngredientStatusLegend(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp),
                    )

                    FadeScrollLazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding =
                            PaddingValues(
                                top = 8.dp,
                                bottom = 80.dp,
                            ),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        IngredientCategoryType.entries.forEach { categoryType ->
                            val items = categorizedIngredients[categoryType] ?: emptyList()

                            if (items.isNotEmpty()) {
                                item {
                                    StorageSection(
                                        title = stringResource(categoryType.labelResId),
                                        items = items,
                                        displayType = ListDisplayType.GRID,
                                        selectedIngredientIds = emptySet(),
                                        onIngredientClick = { ingredient ->
                                            ingredient.id?.let { onNavigateToIngredientEdit(it) }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
