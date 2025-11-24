package com.kjw.fridgerecipe.presentation.ui.screen

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kjw.fridgerecipe.BuildConfig
import com.kjw.fridgerecipe.presentation.navigation.AppNavHost
import com.kjw.fridgerecipe.presentation.navigation.INGREDIENT_DETAIL_BASE_ROUTE
import com.kjw.fridgerecipe.presentation.navigation.NavItem
import com.kjw.fridgerecipe.presentation.navigation.RECIPE_DETAIL_BASE_ROUTE
import com.kjw.fridgerecipe.presentation.navigation.RECIPE_EDIT_BASE_ROUTE
import com.kjw.fridgerecipe.presentation.ui.components.AppBottomNavigationBar
import com.kjw.fridgerecipe.worker.ExpirationCheckWorker

private data class MainAppScreenState(
    val navController: NavHostController,
    val currentRoute: String?,
    val isDetailScreen: Boolean,
    val currentTitle: String
)

@Composable
private fun rememberMainAppScreenState(
    navController: NavHostController = rememberNavController()
): MainAppScreenState {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isIngredientDetailScreen = currentRoute?.startsWith(INGREDIENT_DETAIL_BASE_ROUTE) == true
    val isRecipeDetailScreen = currentRoute?.startsWith(RECIPE_DETAIL_BASE_ROUTE) == true
    val isRecipeEditScreen = currentRoute?.startsWith(RECIPE_EDIT_BASE_ROUTE) == true
    val isDetailScreen = isIngredientDetailScreen || isRecipeDetailScreen || isRecipeEditScreen

    val currentScreen = remember(currentRoute, isDetailScreen) {
        if (isIngredientDetailScreen) NavItem.IngredientDetail
        else if (isRecipeDetailScreen) NavItem.RecipeDetail
        else if (isRecipeEditScreen) NavItem.RecipeEdit
        else listOf(NavItem.Home, NavItem.Ingredients, NavItem.Recipes).find { it.route == currentRoute }
    }

    val currentTitle = currentScreen?.title ?: NavItem.Home.title

    return remember(navController, currentRoute, isDetailScreen, currentTitle) {
        MainAppScreenState(
            navController = navController,
            currentRoute = currentRoute,
            isDetailScreen = isDetailScreen,
            currentTitle = currentTitle
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val screenState = rememberMainAppScreenState()

    var backPressedTime by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current
    val activity = (context as? Activity)

    BackHandler(enabled = screenState.currentRoute == NavItem.Home.route) {
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
                title = {
                    Text(
                        text = screenState.currentTitle,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                navigationIcon = {
                    if (screenState.isDetailScreen) {
                        IconButton(onClick = { screenState.navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로 가기"
                            )
                        }
                    }
                },
                actions = {
                    if (BuildConfig.DEBUG && screenState.currentRoute == NavItem.Home.route) {
                        IconButton(onClick = {
                            val testRequest = OneTimeWorkRequestBuilder<ExpirationCheckWorker>().build()
                            WorkManager.getInstance(context).enqueue(testRequest)
                            Toast.makeText(context, "알림 테스트 실행!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.BugReport, contentDescription = "알림 테스트")
                        }
                    }

                    IconButton(onClick = {
                        Toast.makeText(context, "설정 기능 준비 중입니다.", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "설정")
                    }
                }
            )
        },
        bottomBar = {
            if (!screenState.isDetailScreen) {
                AppBottomNavigationBar(navController = screenState.navController, currentRoute = screenState.currentRoute)
            }
        },
        floatingActionButton = {
            if (screenState.currentRoute == NavItem.Ingredients.route) {
                FloatingActionButton(onClick = {
                    screenState.navController.navigate(INGREDIENT_DETAIL_BASE_ROUTE)
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "재료 추가")
                }
            } else if (screenState.currentRoute == NavItem.Recipes.route) {
                FloatingActionButton(onClick = {
                    screenState.navController.navigate(RECIPE_EDIT_BASE_ROUTE)
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "레시피 추가")
                }
            }
        }
    ) { paddingValues ->

        AppNavHost(
            navController = screenState.navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}