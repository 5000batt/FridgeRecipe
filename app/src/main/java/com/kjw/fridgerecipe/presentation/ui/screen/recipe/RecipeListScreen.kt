package com.kjw.fridgerecipe.presentation.ui.screen.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.kjw.fridgerecipe.presentation.ui.components.recipe.RecipeListItem
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeViewModel

@Composable
fun RecipeListScreen(
    viewModel: RecipeViewModel = hiltViewModel(),
    onRecipeClick: (Long) -> Unit
) {
    val filteredRecipes by viewModel.savedRecipes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val rawSavedRecipes by viewModel.rawSavedRecipes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        CommonSearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.onSearchQueryChanged(it) },
            placeholderText = stringResource(R.string.recipe_search_placeholder),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (rawSavedRecipes.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.SoupKitchen,
                title = stringResource(R.string.recipe_empty_title),
                message = stringResource(R.string.recipe_empty_desc)
            )
        } else {
            if (filteredRecipes.isEmpty() && searchQuery.isNotBlank()) {
                EmptyStateView(
                    icon = Icons.Default.Search,
                    title = stringResource(R.string.recipe_search_empty_title, searchQuery),
                    message = stringResource(R.string.recipe_search_empty_desc)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = filteredRecipes,
                        key = { it.id ?: 0 }
                    ) { recipe ->
                        RecipeListItem(
                            recipe = recipe,
                            onClick = {
                                recipe.id?.let { id ->
                                    onRecipeClick(id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}