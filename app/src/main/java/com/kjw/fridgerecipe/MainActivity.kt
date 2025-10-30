package com.kjw.fridgerecipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
 import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.kjw.fridgerecipe.presentation.navigation.NavItem
import com.kjw.fridgerecipe.presentation.ui.screen.AddIngredientScreen
import com.kjw.fridgerecipe.presentation.ui.screen.IngredientListScreen
import com.kjw.fridgerecipe.presentation.ui.screen.MainScreen
import com.kjw.fridgerecipe.presentation.ui.screen.RecipeScreen
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
                    listOf(
                        NavItem.Home,
                        NavItem.Ingredients,
                        NavItem.Recipes,
                        NavItem.AddIngredient
                    )
                        .find { it.route == currentRoute }
                }
                val currentTitle = currentScreen?.title ?: NavItem.Home.title

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(currentTitle) },
                            navigationIcon = {
                                if (currentRoute == NavItem.AddIngredient.route) {
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
                        if (currentRoute != NavItem.AddIngredient.route) {
                            AppBottomNavigationBar(navController = navController, currentRoute = currentRoute)
                        }
                    },
                    floatingActionButton = {
                        if (currentRoute == NavItem.Ingredients.route) {
                            FloatingActionButton(onClick = {
                                navController.navigate(NavItem.AddIngredient.route)
                            }) {
                                Icon(Icons.Filled.Add, contentDescription = "재료 추가")
                            }
                        }
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = NavItem.Home.route,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable(NavItem.Home.route) { MainScreen() }
                        composable(NavItem.Ingredients.route) { IngredientListScreen() }
                        composable(NavItem.Recipes.route) { RecipeScreen() }
                        composable(NavItem.AddIngredient.route) {
                            AddIngredientScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(navController: NavController, currentRoute: String?) {
    val items = listOf(NavItem.Home, NavItem.Ingredients, NavItem.Recipes)

    NavigationBar {
        items.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationBarItem(
                icon = { screen.icon?.let { Icon(it, contentDescription = screen.label) } },
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