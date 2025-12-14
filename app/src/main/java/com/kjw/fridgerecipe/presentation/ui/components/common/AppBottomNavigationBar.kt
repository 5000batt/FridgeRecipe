package com.kjw.fridgerecipe.presentation.ui.components.common

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.kjw.fridgerecipe.presentation.navigation.MainTab

@Composable
fun AppBottomNavigationBar(navController: NavController, currentRoute: String?) {
    val items = listOf(MainTab.HOME, MainTab.INGREDIENTS, MainTab.RECIPES)

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