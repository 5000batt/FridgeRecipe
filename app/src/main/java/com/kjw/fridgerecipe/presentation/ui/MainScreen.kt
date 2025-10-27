package com.kjw.fridgerecipe.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.presentation.viewmodel.IngredientViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: IngredientViewModel = hiltViewModel()) {
    val ingredients by viewModel.ingredients.collectAsState()

    val oneWeekLater = remember { LocalDate.now().plusDays(7) }

    val categorizedIngredients = remember(ingredients) {
        ingredients
            .filter { it.expirationDate.isBefore(oneWeekLater) }
            .groupBy { it.storageLocation }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("냉장고 현황") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            StorageType.entries.forEach { storageType ->
                val items = categorizedIngredients[storageType] ?: emptyList()

                StorageSection(
                    title = storageType.label,
                    items = items,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "오늘의 추천 레시피",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text("보유 재료 기반 추천 레시피 요약...")
            }
        }
    }
}

@Composable
fun StorageSection(
    title: String,
    items: List<Ingredient>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "$title (${items.size}개)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (items.isEmpty()) {
            Text("재료가 없습니다.")
        } else {
            LazyRow(
                modifier = Modifier.heightIn(min = 80.dp, max = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { ingredient ->
                    IngredientChip(ingredient)
                }
            }
        }
    }
}

@Composable
fun IngredientChip(ingredient: Ingredient) {
    val chipColor = when {
        ingredient.expirationDate.isBefore(LocalDate.now()) -> Color.Red.copy(alpha = 0.4F)
        ingredient.expirationDate.isBefore(LocalDate.now().plusDays(3)) -> Color.Yellow.copy(alpha = 0.4F)
        else -> Color.LightGray
    }

    Column(
        modifier = Modifier
            .size(width = 100.dp, height = 100.dp)
            .background(chipColor, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IngredientIconImage(ingredient)

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = ingredient.name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = ingredient.expirationDate.format(DateTimeFormatter.ofPattern("y.MM.dd")),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun IngredientIconImage(ingredient: Ingredient) {
    val imagePainter = painterResource(id = ingredient.emoticon.iconResId)

    Image(
        painter = imagePainter,
        contentDescription = ingredient.emoticon.description + " 아이콘",
        modifier = Modifier.size(48.dp),
        contentScale = ContentScale.Fit
    )
}