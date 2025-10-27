package com.kjw.fridgerecipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kjw.fridgerecipe.presentation.navigation.BottomNavItem
import com.kjw.fridgerecipe.presentation.ui.IngredientListScreen
import com.kjw.fridgerecipe.presentation.ui.MainScreen
import com.kjw.fridgerecipe.presentation.ui.RecipeScreen
import com.kjw.fridgerecipe.ui.theme.FridgeRecipeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FridgeRecipeTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val currentScreen = remember(currentRoute) {
                    listOf(BottomNavItem.Home, BottomNavItem.Ingredients, BottomNavItem.Recipes)
                        .find { it.route == currentRoute }
                }
                val currentTitle = currentScreen?.title ?: BottomNavItem.Home.title

                Scaffold(
                    bottomBar = { AppBottomNavigationBar(navController = navController, currentRoute = currentRoute) },
                    topBar = { TopAppBar(title = { Text(currentTitle) }) }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = BottomNavItem.Home.route,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable(BottomNavItem.Home.route) { MainScreen() }
                        composable(BottomNavItem.Ingredients.route) { IngredientListScreen() }
                        composable(BottomNavItem.Recipes.route) { RecipeScreen() }
                    }
                }
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(navController: NavController, currentRoute: String?) {
    val items = listOf(BottomNavItem.Home, BottomNavItem.Ingredients, BottomNavItem.Recipes)

    NavigationBar {
        items.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = selected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}