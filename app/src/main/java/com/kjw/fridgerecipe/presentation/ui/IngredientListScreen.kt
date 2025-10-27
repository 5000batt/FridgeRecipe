package com.kjw.fridgerecipe.presentation.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.presentation.viewmodel.IngredientViewModel

@Composable
fun IngredientListScreen(viewModel: IngredientViewModel = hiltViewModel()) {
    val ingredients by viewModel.ingredients.collectAsState()

    Text(text = "재료 목록 화면 (구현 예정)")
}