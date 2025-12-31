package com.kjw.fridgerecipe.presentation.ui.screen.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.presentation.navigation.MainTab
import com.kjw.fridgerecipe.presentation.navigation.RecipeEditRoute
import com.kjw.fridgerecipe.presentation.ui.components.common.BottomNavigationBar
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonFloatingActionButton
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonSearchBar
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonTopBar
import com.kjw.fridgerecipe.presentation.ui.components.common.EmptyStateView
import com.kjw.fridgerecipe.presentation.ui.components.common.FadeScrollLazyColumn
import com.kjw.fridgerecipe.presentation.ui.components.common.LoadingContent
import com.kjw.fridgerecipe.presentation.ui.components.recipe.list.RecipeListItem
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeListViewModel

@Composable
fun RecipeListScreen(
    viewModel: RecipeListViewModel = hiltViewModel(),
    onNavigateToRecipeDetail: (Long) -> Unit,
    onNavigateToRecipeEdit: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMainTab: (MainTab) -> Unit
) {
    val recipeList by viewModel.recipeList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sortType by viewModel.sortType.collectAsState()
    val listState = androidx.compose.runtime.key(sortType) {
        rememberLazyListState()
    }


    LoadingContent(isLoading = isLoading) {
        Scaffold(
            topBar = {
                CommonTopBar(
                    title = stringResource(MainTab.RECIPES.titleResId),
                    onSettingClick = onNavigateToSettings
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    currentTab = MainTab.RECIPES,
                    onTabSelected = onNavigateToMainTab
                )
            },
            floatingActionButton = {
                CommonFloatingActionButton(
                    text = stringResource(R.string.fab_add_recipe),
                    icon = Icons.Filled.Add,
                    state = listState,
                    onClick = { onNavigateToRecipeEdit(RecipeEditRoute.DEFAULT_ID) }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                CommonSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    placeholderText = stringResource(R.string.recipe_search_placeholder),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sortOptions = listOf(
                        RecipeListViewModel.SortType.LATEST to R.string.recipe_sort_latest,
                        RecipeListViewModel.SortType.OLDEST to R.string.recipe_sort_oldest,
                        RecipeListViewModel.SortType.MATCH_RATE to R.string.recipe_sort_match_rate,
                    )

                    sortOptions.forEach { (type, labelRes) ->
                        FilterChip(
                            selected = sortType == type,
                            onClick = { viewModel.onSortTypeChanged(type) },
                            label = { Text(stringResource(labelRes)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                if (recipeList.isEmpty()) {
                    if (searchQuery.isNotBlank()) {
                        EmptyStateView(
                            icon = Icons.Default.Search,
                            title = stringResource(R.string.recipe_search_empty_title, searchQuery),
                            message = stringResource(R.string.recipe_search_empty_desc)
                        )
                    } else {
                        EmptyStateView(
                            icon = Icons.Default.SoupKitchen,
                            title = stringResource(R.string.recipe_empty_title),
                            message = stringResource(R.string.recipe_empty_desc)
                        )
                    }
                } else {
                    FadeScrollLazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = 80.dp
                        ),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = recipeList,
                            key = { it.recipe.id ?: 0 }
                        ) { item ->
                            RecipeListItem(
                                recipe = item.recipe,
                                matchPercentage = item.matchPercentage,
                                isCookable = item.isCookable,
                                onRecipeClick = {
                                    item.recipe.id?.let { id ->
                                        onNavigateToRecipeDetail(id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}