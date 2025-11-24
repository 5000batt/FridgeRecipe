package com.kjw.fridgerecipe.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kjw.fridgerecipe.presentation.ui.components.IngredientListItem
import com.kjw.fridgerecipe.presentation.ui.components.RecipeInfoRow
import com.kjw.fridgerecipe.presentation.ui.components.RecipeStepItem
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeManageViewModel

@Composable
fun RecipeDetailScreen(
    onNavigateToRecipeEdit: (Long) -> Unit,
    viewModel: RecipeManageViewModel = hiltViewModel(),
    recipeId: Long
) {
    val selectedRecipe by viewModel.selectedRecipe.collectAsState()

    LaunchedEffect(recipeId) {
        viewModel.loadRecipeById(recipeId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSelectedRecipe()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (selectedRecipe == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val recipe = selectedRecipe!!

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                if (recipe.imageUri != null) {
                    AsyncImage(
                        model = recipe.imageUri,
                        contentDescription = "레시피 완성 사진",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                RecipeInfoRow(recipe = recipe)

                Divider(modifier = Modifier.padding(vertical = 24.dp))

                Text(
                    text = "재료",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                recipe.ingredients.forEach { ingredient ->
                    IngredientListItem(ingredient)
                }

                Divider(modifier = Modifier.padding(vertical = 24.dp))

                Text(
                    text = "조리 순서",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                recipe.steps.forEach { step ->
                    RecipeStepItem(step)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            Button(
                onClick = {
                    recipe.id?.let { id ->
                        onNavigateToRecipeEdit(id)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("수정")
            }
        }
    }
}