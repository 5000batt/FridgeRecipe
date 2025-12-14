package com.kjw.fridgerecipe.presentation.ui.screen

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kjw.fridgerecipe.BuildConfig
import com.kjw.fridgerecipe.presentation.navigation.AppNavHost
import com.kjw.fridgerecipe.presentation.navigation.Destination
import com.kjw.fridgerecipe.presentation.navigation.DetailDestination
import com.kjw.fridgerecipe.presentation.navigation.MainTab
import com.kjw.fridgerecipe.presentation.ui.components.common.AppBottomNavigationBar
import com.kjw.fridgerecipe.presentation.util.CustomSnackbarVisuals
import com.kjw.fridgerecipe.presentation.util.SnackbarType
import com.kjw.fridgerecipe.worker.ExpirationCheckWorker
import kotlinx.coroutines.launch

private data class MainAppScreenState(
    val navController: NavHostController,
    val currentDestination: Destination?,
    val currentTitle: String
) {
    val showBottomBar: Boolean
        get() = currentDestination is MainTab

    val showBackButton: Boolean
        get() = currentDestination is DetailDestination
}

@Composable
private fun rememberMainAppScreenState(
    navController: NavHostController = rememberNavController()
): MainAppScreenState {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentDestination: Destination? = remember(currentRoute) {
        MainTab.getByRoute(currentRoute) ?: DetailDestination.getByRoute(currentRoute)
    }

    val currentTitle = remember(currentDestination, navBackStackEntry) {
        when(currentDestination) {
            is MainTab -> currentDestination.title
            is DetailDestination.IngredientEdit -> {
                val id = navBackStackEntry?.arguments?.getLong(DetailDestination.IngredientEdit.ARG_ID)
                    ?: DetailDestination.IngredientEdit.DEFAULT_ID
                currentDestination.getTitle(id == DetailDestination.IngredientEdit.DEFAULT_ID)
            }
            is DetailDestination -> currentDestination.title
            else -> ""
        }
    }

    return remember(navController, currentDestination, currentTitle) {
        MainAppScreenState(navController, currentDestination, currentTitle)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val screenState = rememberMainAppScreenState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val showSnackbar: (String, SnackbarType) -> Unit = { message, type ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                CustomSnackbarVisuals(
                    message = message,
                    type = type
                )
            )
        }
    }

    var backPressedTime by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current
    val activity = (context as? Activity)

    BackHandler(enabled = screenState.currentDestination?.route == MainTab.HOME.route) {
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
                    if (screenState.showBackButton) {
                        IconButton(onClick = { screenState.navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로 가기"
                            )
                        }
                    }
                },
                actions = {
                    if (BuildConfig.DEBUG && screenState.currentDestination?.route == MainTab.HOME.route) {
                        IconButton(onClick = {
                            val testRequest = OneTimeWorkRequestBuilder<ExpirationCheckWorker>().build()
                            WorkManager.getInstance(context).enqueue(testRequest)
                            Toast.makeText(context, "알림 테스트 실행!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.BugReport, contentDescription = "알림 테스트")
                        }
                    }

                    if (!screenState.showBackButton) {
                        IconButton(onClick = { screenState.navController.navigate(MainTab.SETTINGS.route)}) {
                            Icon(Icons.Default.Settings, contentDescription = "설정")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (screenState.showBottomBar) {
                AppBottomNavigationBar(navController = screenState.navController, currentRoute = screenState.currentDestination?.route)
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val customVisuals = data.visuals as? CustomSnackbarVisuals
                val type = customVisuals?.type ?: SnackbarType.INFO

                val isError = type == SnackbarType.ERROR

                val containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                val contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                val icon = if (isError) Icons.Default.Error else Icons.Default.CheckCircle

                Snackbar(
                    containerColor = containerColor,
                    contentColor = contentColor,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = data.visuals.message,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            when (screenState.currentDestination) {
                MainTab.INGREDIENTS -> {
                    ExtendedFloatingActionButton(
                        onClick = { screenState.navController.navigate(DetailDestination.IngredientEdit.createRoute()) },
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        text = { Text("재료 추가") },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                MainTab.RECIPES -> {
                    ExtendedFloatingActionButton(
                        onClick = { screenState.navController.navigate(DetailDestination.RecipeEdit.createRoute()) },
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        text = { Text("레시피 추가") }
                    )
                }
                else -> {}
            }
        }
    ) { paddingValues ->

        AppNavHost(
            navController = screenState.navController,
            modifier = Modifier.padding(paddingValues),
            onShowSnackbar = showSnackbar
        )
    }
}