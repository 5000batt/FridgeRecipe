package com.kjw.fridgerecipe.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kjw.fridgerecipe.presentation.ui.screen.IngredientEditScreen
import com.kjw.fridgerecipe.presentation.ui.screen.IngredientListScreen
import com.kjw.fridgerecipe.presentation.ui.screen.HomeScreen
import com.kjw.fridgerecipe.presentation.ui.screen.RecipeDetailScreen
import com.kjw.fridgerecipe.presentation.ui.screen.RecipeEditScreen
import com.kjw.fridgerecipe.presentation.ui.screen.RecipeListScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavItem.Home.route,
        modifier = modifier
    ) {
        composable(NavItem.Home.route) {
            HomeScreen(
                onNavigateToRecipeDetail = { recipeId ->
                    navController.navigate("$RECIPE_DETAIL_BASE_ROUTE/$recipeId")
                }
            )
        }
        composable(NavItem.Ingredients.route) {
            IngredientListScreen(
                onIngredientClick = { ingredientId ->
                    navController.navigate("$INGREDIENT_EDIT_BASE_ROUTE?$INGREDIENT_ID_ARG=$ingredientId")
                }
            )
        }
        composable(
            route = INGREDIENT_EDIT_ROUTE_PATTERN,
            arguments = listOf(navArgument(INGREDIENT_ID_ARG) {
                type = NavType.LongType
                defaultValue = INGREDIENT_ID_DEFAULT
            })
        ) { backStackEntry ->
            val ingredientId = backStackEntry.arguments?.getLong(INGREDIENT_ID_ARG) ?: INGREDIENT_ID_DEFAULT

            IngredientEditScreen(
                onNavigateBack = { navController.popBackStack() },
                ingredientId = ingredientId
            )
        }
        composable(NavItem.Recipes.route) {
            RecipeListScreen(
                onRecipeClick = { recipeId ->
                    navController.navigate("$RECIPE_DETAIL_BASE_ROUTE/$recipeId")
                }
            )
        }
        composable(
            route = RECIPE_DETAIL_ROUTE_PATTERN,
            arguments = listOf(navArgument(RECIPE_ID_ARG) {
                type = NavType.LongType
            })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong(RECIPE_ID_ARG)

            if (recipeId != null) {
                RecipeDetailScreen(
                    onNavigateToRecipeEdit = { recipeId ->
                        navController.navigate("$RECIPE_EDIT_BASE_ROUTE?$RECIPE_ID_ARG=$recipeId")
                    },
                    recipeId = recipeId
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
        composable(
            route = RECIPE_EDIT_ROUTE_PATTERN,
            arguments = listOf(navArgument(RECIPE_ID_ARG) {
                type = NavType.LongType
                defaultValue = RECIPE_ID_DEFAULT
            })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong(RECIPE_ID_ARG) ?: RECIPE_ID_DEFAULT

            RecipeEditScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToList = {
                    navController.popBackStack(
                        route = NavItem.Recipes.route,
                        inclusive = false
                    )
                },
                recipeId = recipeId
            )
        }
    }
}