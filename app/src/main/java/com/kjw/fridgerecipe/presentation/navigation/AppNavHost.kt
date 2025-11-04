package com.kjw.fridgerecipe.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kjw.fridgerecipe.presentation.ui.screen.IngredientDetailScreen
import com.kjw.fridgerecipe.presentation.ui.screen.IngredientListScreen
import com.kjw.fridgerecipe.presentation.ui.screen.HomeScreen
import com.kjw.fridgerecipe.presentation.ui.screen.RecipeDetailScreen
import com.kjw.fridgerecipe.presentation.ui.screen.RecipeScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onIngredientClick: (Long) -> Unit,
    onRecipeClick: (Long) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = NavItem.Home.route,
        modifier = modifier
    ) {
        composable(NavItem.Home.route) {
            HomeScreen(onIngredientClick = onIngredientClick)
        }
        composable(NavItem.Ingredients.route) {
            IngredientListScreen(onIngredientClick = onIngredientClick)
        }
        composable(
            route = INGREDIENT_DETAIL_ROUTE_PATTERN,
            arguments = listOf(navArgument(INGREDIENT_ID_ARG) {
                type = NavType.LongType
                defaultValue = INGREDIENT_ID_DEFAULT
            })
        ) { backStackEntry ->
            val ingredientId = backStackEntry.arguments?.getLong(INGREDIENT_ID_ARG) ?: INGREDIENT_ID_DEFAULT

            IngredientDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                ingredientId = ingredientId
            )
        }
        composable(NavItem.Recipes.route) {
            RecipeScreen(onRecipeClick = onRecipeClick)
        }
        composable(
            route = RECIPE_DETAIL_ROUTE_PATTERN,
            arguments = listOf(navArgument(RECIPE_ID_ARG) {
                type = NavType.LongType
            })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong(RECIPE_ID_ARG)
            RecipeDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                recipeId = recipeId
            )
        }
    }
}