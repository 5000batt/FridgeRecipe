package com.kjw.fridgerecipe.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
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
    onShowAd: (onReward: () -> Unit) -> Unit,
    onNavigateToMainTab: (MainTab) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        // --- Main Tabs ---
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToRecipeDetail = { id -> navController.navigate(RecipeDetailRoute(id)) },
                onNavigateToIngredientEdit = { navController.navigate(IngredientEditRoute()) },
                onNavigateToSettings = { navController.navigate(SettingsRoute) },
                onShowAd = onShowAd,
                onShowSnackbar = onShowSnackbar,
                onNavigateToMainTab = onNavigateToMainTab
            )
        }

        composable<IngredientListRoute> {
            IngredientListScreen(
                onNavigateToIngredientEdit = { id -> navController.navigate(IngredientEditRoute(id)) },
                onNavigateToSettings = { navController.navigate(SettingsRoute) },
                onNavigateToMainTab = onNavigateToMainTab
            )
        }

        composable<RecipeListRoute> {
            RecipeListScreen(
                onNavigateToRecipeDetail = { id -> navController.navigate(RecipeDetailRoute(id)) },
                onNavigateToRecipeEdit = { navController.navigate(RecipeEditRoute()) },
                onNavigateToSettings = { navController.navigate(SettingsRoute) },
                onNavigateToMainTab = onNavigateToMainTab
            )
        }

        // --- Details ---
        composable<IngredientEditRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<IngredientEditRoute>()

            IngredientEditScreen(
                onNavigateBack = { navController.popBackStack() },
                ingredientId = route.ingredientId,
                onShowSnackbar = onShowSnackbar
            )
        }

        composable<RecipeDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<RecipeDetailRoute>()

            RecipeDetailScreen(
                onNavigateToRecipeEdit = { id -> navController.navigate(RecipeEditRoute(id)) },
                onNavigateBack = { navController.popBackStack() },
                recipeId = route.recipeId
            )
        }

        composable<RecipeEditRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<RecipeEditRoute>()

            RecipeEditScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRecipeList = {
                    navController.popBackStack<RecipeListRoute>(inclusive = false)
                },
                recipeId = route.recipeId,
                onShowSnackbar = onShowSnackbar
            )
        }

        composable<SettingsRoute> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onShowSnackbar = onShowSnackbar
            )
        }
    }
}