package com.kjw.fridgerecipe.presentation.ui.screen

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeViewModel

@Composable
fun RecipeScreen(
    viewModel: RecipeViewModel = hiltViewModel(),
    onRecipeClick: (Long) -> Unit
) {
    val savedRecipes by viewModel.savedRecipes.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (savedRecipes.isEmpty()) {
            item {
                Text(text = "저장된 레시피가 없습니다. 홈 화면에서 AI 추천을 받아보세요.")
            }
        } else {
            items(savedRecipes) { recipe ->
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

@Composable
fun RecipeListItem(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(recipe.servings, style = MaterialTheme.typography.bodyMedium)
                Text(recipe.time, style = MaterialTheme.typography.bodyMedium)
                Text(recipe.level.label, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}