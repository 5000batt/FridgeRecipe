package com.kjw.fridgerecipe.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.presentation.ui.common.ListDisplayType

@Composable
fun StorageSection(
    title: String,
    items: List<Ingredient>,
    displayType: ListDisplayType,
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
            when (displayType) {
                ListDisplayType.ROW -> {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(min = 80.dp, max = 100.dp)
                    ) {
                        items(items) { ingredient ->
                            IngredientChip(ingredient)
                        }
                    }
                }
                ListDisplayType.GRID -> {
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .height(208.dp)
                            .fillMaxWidth()
                    ) {
                        items(items) { ingredient ->
                            IngredientChip(ingredient)
                        }
                    }
                }
            }
        }
    }
}