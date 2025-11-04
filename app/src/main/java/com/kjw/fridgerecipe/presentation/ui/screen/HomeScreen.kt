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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.presentation.ui.common.ListDisplayType
import com.kjw.fridgerecipe.presentation.ui.components.RecipeCard
import com.kjw.fridgerecipe.presentation.ui.components.StorageSection
import com.kjw.fridgerecipe.presentation.viewmodel.IngredientViewModel
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    ingredientViewModel: IngredientViewModel = hiltViewModel(),
    recipeViewModel: RecipeViewModel = hiltViewModel(),
    onIngredientClick: (Long) -> Unit
) {
    val ingredients by ingredientViewModel.ingredients.collectAsState()

    val oneWeekLater = remember { LocalDate.now().plusDays(7) }

    val categorizedIngredients = remember(ingredients) {
        ingredients
            .filter { it.expirationDate.isBefore(oneWeekLater) }
            .groupBy { it.storageLocation }
    }

    val recipe by recipeViewModel.recipe.collectAsState()
    val isRecipeLoading by recipeViewModel.isRecipeLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        StorageType.entries.forEach { storageType ->
            val items = categorizedIngredients[storageType] ?: emptyList()

            StorageSection(
                title = storageType.label,
                items = items,
                displayType = ListDisplayType.ROW,
                modifier = Modifier.padding(vertical = 8.dp),
                onIngredientClick = onIngredientClick
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "오늘의 추천 레시피",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { recipeViewModel.fetchRecipes() }
            ) {
                Text(if (recipe == null) "레시피 추천 받기" else "다른 추천 받기")
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isRecipeLoading) {
                CircularProgressIndicator()
            } else if (recipe == null) {
                Text("추천 버튼을 눌러주세요.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    RecipeCard(recipe = recipe!!)
                }
            }
        }
    }
}