package com.kjw.fridgerecipe.presentation.ui.screen.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonSearchBar
import com.kjw.fridgerecipe.presentation.ui.components.common.EmptyStateView
import com.kjw.fridgerecipe.presentation.ui.components.common.LoadingContent
import com.kjw.fridgerecipe.presentation.ui.components.recipe.RecipeListItem
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeListViewModel

@Composable
fun RecipeListScreen(
    viewModel: RecipeListViewModel = hiltViewModel(),
    onNavigateToRecipeDetail: (Long) -> Unit
) {
    val recipeList by viewModel.savedRecipes.collectAsState()
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
                placeholderText = stringResource(R.string.recipe_search_placeholder),
                modifier = Modifier.fillMaxWidth()
            )

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
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = recipeList,
                        key = { it.id ?: 0 }
                    ) { recipe ->
                        RecipeListItem(
                            recipe = recipe,
                            onRecipeClick = {
                                recipe.id?.let { id ->
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