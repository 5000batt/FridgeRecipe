package com.kjw.fridgerecipe.presentation.ui.screen

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kjw.fridgerecipe.presentation.navigation.AppNavHost
import com.kjw.fridgerecipe.presentation.navigation.INGREDIENT_DETAIL_BASE_ROUTE
import com.kjw.fridgerecipe.presentation.navigation.INGREDIENT_ID_ARG
import com.kjw.fridgerecipe.presentation.navigation.NavItem
import com.kjw.fridgerecipe.presentation.navigation.RECIPE_DETAIL_BASE_ROUTE
import com.kjw.fridgerecipe.presentation.navigation.RECIPE_EDIT_BASE_ROUTE
import com.kjw.fridgerecipe.presentation.navigation.RECIPE_ID_ARG
import com.kjw.fridgerecipe.presentation.ui.components.AppBottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isIngredientDetailScreen = currentRoute?.startsWith(INGREDIENT_DETAIL_BASE_ROUTE) == true
    val isRecipeDetailScreen = currentRoute?.startsWith(RECIPE_DETAIL_BASE_ROUTE) == true
    val isRecipeEditScreen = currentRoute?.startsWith(RECIPE_EDIT_BASE_ROUTE) == true
    val currentScreen = remember(currentRoute, isIngredientDetailScreen) {
        if (isIngredientDetailScreen) {
            NavItem.IngredientDetail
        } else if (isRecipeDetailScreen) {
            NavItem.RecipeDetail
        } else if (isRecipeEditScreen) {
            NavItem.RecipeEdit
        } else {
            listOf(
                NavItem.Home,
                NavItem.Ingredients,
                NavItem.Recipes
            ).find { it.route == currentRoute }
        }
    }
    val currentTitle = currentScreen?.title ?: NavItem.Home.title

    var backPressedTime by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current
    val activity = (context as? Activity)

    BackHandler(enabled = currentRoute == NavItem.Home.route) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime > 2000L) {
            backPressedTime = currentTime
            Toast.makeText(context, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        } else {
            activity?.finish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTitle) },
                navigationIcon = {
                    if (isIngredientDetailScreen || isRecipeDetailScreen || isRecipeEditScreen) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로 가기"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!(isIngredientDetailScreen || isRecipeDetailScreen || isRecipeEditScreen)) {
                AppBottomNavigationBar(navController = navController, currentRoute = currentRoute)
            }
        },
        floatingActionButton = {
            if (currentRoute == NavItem.Ingredients.route) {
                FloatingActionButton(onClick = {
                    navController.navigate(INGREDIENT_DETAIL_BASE_ROUTE)
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "재료 추가")
                }
            } else if (currentRoute == NavItem.Recipes.route) {
                FloatingActionButton(onClick = {
                    navController.navigate(RECIPE_EDIT_BASE_ROUTE)
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "레시피 추가")
                }
            }
        }
    ) { paddingValues ->
        val onNavigateToIngredientDetail: (Long) -> Unit = { ingredientId ->
            navController.navigate("$INGREDIENT_DETAIL_BASE_ROUTE?$INGREDIENT_ID_ARG=$ingredientId")
        }

        val onNavigateToRecipeDetail: (Long) -> Unit = { recipeId ->
            navController.navigate("$RECIPE_DETAIL_BASE_ROUTE/$recipeId")
        }

        val onNavigateToRecipeEdit: (Long) -> Unit = { recipeId ->
            navController.navigate("$RECIPE_EDIT_BASE_ROUTE?$RECIPE_ID_ARG=$recipeId")
        }

        val onNavigateToList: () -> Unit = {
            navController.popBackStack(
                route = NavItem.Recipes.route,
                inclusive = false
            )
        }

        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(paddingValues),
            onNavigateToIngredientDetail = onNavigateToIngredientDetail,
            onNavigateToRecipeDetail = onNavigateToRecipeDetail,
            onNavigateToRecipeEdit = onNavigateToRecipeEdit,
            onNavigateToList = onNavigateToList
        )
    }
}