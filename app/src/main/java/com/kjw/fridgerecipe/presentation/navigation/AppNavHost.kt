package com.kjw.fridgerecipe.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kjw.fridgerecipe.presentation.ui.screen.ingredient.IngredientEditScreen
import com.kjw.fridgerecipe.presentation.ui.screen.ingredient.IngredientListScreen
import com.kjw.fridgerecipe.presentation.ui.screen.home.HomeScreen
import com.kjw.fridgerecipe.presentation.ui.screen.recipe.RecipeDetailScreen
import com.kjw.fridgerecipe.presentation.ui.screen.recipe.RecipeEditScreen
import com.kjw.fridgerecipe.presentation.ui.screen.recipe.RecipeListScreen
import com.kjw.fridgerecipe.presentation.ui.screen.settings.SettingsScreen
import com.kjw.fridgerecipe.presentation.util.SnackbarType

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onShowSnackbar: (String, SnackbarType) -> Unit,
    onShowAd: (onReward: () -> Unit) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = MainTab.HOME.route,
        modifier = modifier,
    ) {
        // --- Main Tabs ---
        composable(MainTab.HOME.route) {
            HomeScreen(
                onNavigateToRecipeDetail = { recipeId ->
                    navController.navigate(DetailDestination.RecipeDetail.createRoute(recipeId))
                },
                onNavigateToIngredientAdd = {
                    navController.navigate(DetailDestination.IngredientEdit.createRoute())
                },
                onShowAd = onShowAd
            )
        }
        composable(MainTab.INGREDIENTS.route) {
            IngredientListScreen(
                onIngredientClick = { ingredientId ->
                    navController.navigate(DetailDestination.IngredientEdit.createRoute(ingredientId))
                }
            )
        }
        composable(MainTab.RECIPES.route) {
            RecipeListScreen(
                onRecipeClick = { recipeId ->
                    navController.navigate(DetailDestination.RecipeDetail.createRoute(recipeId))
                }
            )
        }

        // --- Details ---
        composable(
            route = DetailDestination.IngredientEdit.route,
            arguments = DetailDestination.IngredientEdit.arguments
        ) { backStackEntry ->
            val ingredientId = backStackEntry.arguments?.getLong(DetailDestination.IngredientEdit.ARG_ID)
                ?: DetailDestination.IngredientEdit.DEFAULT_ID

            IngredientEditScreen(
                onNavigateBack = { navController.popBackStack() },
                ingredientId = ingredientId,
                onShowSnackbar = onShowSnackbar
            )
        }

        composable(
            route = DetailDestination.RecipeDetail.route,
            arguments = DetailDestination.RecipeDetail.arguments
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong(DetailDestination.RecipeDetail.ARG_ID)

            if (recipeId != null) {
                RecipeDetailScreen(
                    onNavigateToRecipeEdit = { id ->
                        navController.navigate(DetailDestination.RecipeEdit.createRoute(id))
                    },
                    recipeId = recipeId
                )
            } else {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(
            route = DetailDestination.RecipeEdit.route,
            arguments = DetailDestination.RecipeEdit.arguments
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong(DetailDestination.RecipeEdit.ARG_ID)
                ?: DetailDestination.RecipeEdit.DEFAULT_ID

            RecipeEditScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToList = {
                    navController.popBackStack(route = MainTab.RECIPES.route, inclusive = false)
                },
                recipeId = recipeId,
                onShowSnackbar = onShowSnackbar
            )
        }

        composable(DetailDestination.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onShowSnackbar = onShowSnackbar
            )
        }
    }
}