package com.kjw.fridgerecipe.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.ui.graphics.vector.ImageVector

const val INGREDIENT_ID_ARG = "ingredientId"
const val INGREDIENT_ID_DEFAULT = -1L
const val INGREDIENT_EDIT_BASE_ROUTE = "INGREDIENT_EDIT"
const val INGREDIENT_EDIT_ROUTE_PATTERN = "$INGREDIENT_EDIT_BASE_ROUTE?$INGREDIENT_ID_ARG={$INGREDIENT_ID_ARG}"

const val RECIPE_ID_ARG = "recipeId"
const val RECIPE_ID_DEFAULT = -1L
const val RECIPE_DETAIL_BASE_ROUTE = "RECIPE_DETAIL"
const val RECIPE_DETAIL_ROUTE_PATTERN = "$RECIPE_DETAIL_BASE_ROUTE/{$RECIPE_ID_ARG}"
const val RECIPE_EDIT_BASE_ROUTE = "RECIPE_EDIT"
const val RECIPE_EDIT_ROUTE_PATTERN = "$RECIPE_EDIT_BASE_ROUTE?$RECIPE_ID_ARG={$RECIPE_ID_ARG}"

sealed class NavItem(val route: String, val icon: ImageVector?, val label: String, val title: String) {
    data object Home : NavItem("HOME", Icons.Default.Home, "홈", "Fridge Recipe")
    data object Ingredients : NavItem("INGREDIENTS", Icons.Default.Kitchen, "재료", "전체 재료 목록")
    data object Recipes : NavItem("RECIPES", Icons.Default.RestaurantMenu, "레시피", "레시피")
    data object IngredientEdit : NavItem(INGREDIENT_EDIT_BASE_ROUTE, null, "재료 상세화면", "재료 상세화면")
    data object RecipeDetail : NavItem(RECIPE_DETAIL_BASE_ROUTE, null, "레시피 상세화면", "레시피 상세화면")
    data object RecipeEdit : NavItem(RECIPE_EDIT_BASE_ROUTE, null, "레시피 편집화면", "레시피 편집화면")
}