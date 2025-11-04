package com.kjw.fridgerecipe.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.vector.ImageVector

const val INGREDIENT_ID_ARG = "ingredientId"
const val INGREDIENT_ID_DEFAULT = -1L
const val INGREDIENT_DETAIL_BASE_ROUTE = "INGREDIENT_DETAIL"
const val INGREDIENT_DETAIL_ROUTE_PATTERN = "$INGREDIENT_DETAIL_BASE_ROUTE?$INGREDIENT_ID_ARG={$INGREDIENT_ID_ARG}"

sealed class NavItem(val route: String, val icon: ImageVector?, val label: String, val title: String) {
    data object Home : NavItem("HOME", Icons.Default.Home, "홈", "메인")
    data object Ingredients : NavItem("INGREDIENTS", Icons.Default.List, "재료", "전체 재료 목록")
    data object Recipes : NavItem("RECIPES", Icons.Default.Menu, "레시피", "레시피")
    data object IngredientDetail : NavItem(INGREDIENT_DETAIL_BASE_ROUTE, null, "재료 상세화면", "재료 상세화면")
}