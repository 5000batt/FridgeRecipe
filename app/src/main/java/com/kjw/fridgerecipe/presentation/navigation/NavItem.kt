package com.kjw.fridgerecipe.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(val route: String, val icon: ImageVector?, val label: String, val title: String) {
    data object Home : NavItem("HOME", Icons.Default.Home, "홈", "메인")
    data object Ingredients : NavItem("INGREDIENTS", Icons.Default.List, "재료", "전체 재료 목록")
    data object Recipes : NavItem("RECIPES", Icons.Default.Menu, "추천 레시피", "추천 레시피")
    data object AddIngredient : NavItem("ADD_INGREDIENT", null, "재료 추가", "재료 추가")
}