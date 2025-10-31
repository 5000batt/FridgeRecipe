package com.kjw.fridgerecipe.presentation.ui.screen

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kjw.fridgerecipe.presentation.navigation.AppNavHost
import com.kjw.fridgerecipe.presentation.navigation.INGREDIENT_DETAIL_BASE_ROUTE
import com.kjw.fridgerecipe.presentation.navigation.INGREDIENT_ID_ARG
import com.kjw.fridgerecipe.presentation.navigation.NavItem
import com.kjw.fridgerecipe.presentation.ui.components.AppBottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isDetailScreen = currentRoute?.startsWith(INGREDIENT_DETAIL_BASE_ROUTE) == true
    val currentScreen = remember(currentRoute, isDetailScreen) {
        if (isDetailScreen) {
            NavItem.IngredientDetail
        } else {
            listOf(
                NavItem.Home,
                NavItem.Ingredients,
                NavItem.Recipes
            ).find { it.route == currentRoute }
        }
    }
    val currentTitle = currentScreen?.title ?: NavItem.Home.title

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTitle) },
                navigationIcon = {
                    if (isDetailScreen) {
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
            if (!isDetailScreen) {
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
            }
        }
    ) { paddingValues ->
        val onIngredientClick: (Long) -> Unit = { ingredientId ->
            navController.navigate("$INGREDIENT_DETAIL_BASE_ROUTE?$INGREDIENT_ID_ARG=$ingredientId")
        }

        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(paddingValues),
            onIngredientClick = onIngredientClick
        )
    }
}