package com.kjw.fridgerecipe.presentation.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.presentation.ui.common.ListDisplayType
import com.kjw.fridgerecipe.presentation.ui.components.StorageSection
import com.kjw.fridgerecipe.presentation.viewmodel.IngredientViewModel

@Composable
fun IngredientListScreen(viewModel: IngredientViewModel = hiltViewModel()) {
    val ingredients by viewModel.ingredients.collectAsState()

    val categorizedIngredients = remember(ingredients) {
        ingredients
            .groupBy { it.storageLocation }
    }

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
                displayType = ListDisplayType.GRID,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}