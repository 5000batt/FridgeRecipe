package com.kjw.fridgerecipe.presentation.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.presentation.ui.components.RecipeCard
import com.kjw.fridgerecipe.presentation.viewmodel.IngredientViewModel

@Composable
fun RecipeDetailScreen(
    recipeId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: IngredientViewModel = hiltViewModel()
) {
    val recipe by viewModel.selectedRecipe.collectAsState()

    LaunchedEffect(recipeId) {
        if (recipeId != null) {
            viewModel.loadRecipeById(recipeId)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSelectedRecipe()
        }
    }

    Box(
      modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
          .verticalScroll(rememberScrollState())
    ) {
        recipe?.let {
            RecipeCard(recipe = it)
        }
    }
}