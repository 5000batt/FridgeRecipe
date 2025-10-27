package com.kjw.fridgerecipe.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String, val title: String) {
    data object Home : BottomNavItem("HOME", Icons.Default.Home, "홈", "메인")
    data object Ingredients : BottomNavItem("INGREDIENTS", Icons.Default.List, "재료", "전체 재료 목록")
    data object Recipes : BottomNavItem("RECIPES", Icons.Default.Menu, "추천 레시피", "추천 레시피")
}