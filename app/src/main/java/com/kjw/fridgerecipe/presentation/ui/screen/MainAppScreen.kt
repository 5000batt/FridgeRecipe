package com.kjw.fridgerecipe.presentation.ui.screen

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import kotlinx.coroutines.delay
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

            val job = launch {
                snackbarHostState.showSnackbar(
                    CustomSnackbarVisuals(
                        message = message,
                        type = type,
                        duration = SnackbarDuration.Indefinite
                    )
                )
            }

            delay(2000L)

            job.cancel()
        }
    }

    var backPressedTime by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current
    val activity = (context as? Activity)

    BackHandler(enabled = screenState.currentDestination?.route == MainTab.HOME.route) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime > 2000L) {
            backPressedTime = currentTime
            showSnackbar("한 번 더 누르면 종료됩니다.", SnackbarType.INFO)
        } else {
            activity?.finish()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                                showSnackbar("알림 테스트 실행!", SnackbarType.SUCCESS)
                            }) {
                                Icon(Icons.Default.BugReport, contentDescription = "알림 테스트")
                            }
                        }

                        if (!screenState.showBackButton) {
                            IconButton(onClick = { screenState.navController.navigate(DetailDestination.Settings.createRoute())}) {
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .systemBarsPadding()
        ) { data ->
            val customVisuals = data.visuals as? CustomSnackbarVisuals

            val (containerColor, contentColor, icon) = when (customVisuals?.type) {
                SnackbarType.ERROR -> Triple(
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.colorScheme.onErrorContainer,
                    Icons.Default.ErrorOutline
                )
                SnackbarType.SUCCESS -> Triple(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    Icons.Default.CheckCircle
                )
                else -> Triple(
                    MaterialTheme.colorScheme.inverseSurface,
                    MaterialTheme.colorScheme.inverseOnSurface,
                    Icons.Default.Info
                )
            }

            Snackbar(
                modifier = Modifier
                    .padding(12.dp)
                    .widthIn(max = 400.dp),
                containerColor = containerColor.copy(alpha = 0.9f),
                contentColor = contentColor,
                shape = RoundedCornerShape(28.dp),
                action = {
                    data.visuals.actionLabel?.let { actionLabel ->
                        TextButton(onClick = { data.performAction() }) {
                            Text(
                                text = actionLabel,
                                color = contentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = data.visuals.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}