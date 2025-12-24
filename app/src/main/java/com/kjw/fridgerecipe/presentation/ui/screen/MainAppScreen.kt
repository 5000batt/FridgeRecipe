package com.kjw.fridgerecipe.presentation.ui.screen

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kjw.fridgerecipe.BuildConfig
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.presentation.navigation.AppNavHost
import com.kjw.fridgerecipe.presentation.navigation.IngredientEditRoute
import com.kjw.fridgerecipe.presentation.navigation.MainTab
import com.kjw.fridgerecipe.presentation.navigation.RecipeDetailRoute
import com.kjw.fridgerecipe.presentation.navigation.RecipeEditRoute
import com.kjw.fridgerecipe.presentation.navigation.SettingsRoute
import com.kjw.fridgerecipe.presentation.util.CustomSnackbarVisuals
import com.kjw.fridgerecipe.presentation.util.SnackbarType
import com.kjw.fridgerecipe.worker.ExpirationCheckWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    onShowAd: (onReward: () -> Unit) -> Unit
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = (context as? Activity)

    // 현재 화면 상태
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 현재 탭
    val currentTab = MainTab.entries.find { it.isSelected(currentDestination) }

    // 타이틀
    val topBarTitle = @Composable {
        when {
            currentTab != null -> stringResource(currentTab.titleResId)

            currentDestination?.hasRoute<IngredientEditRoute>() == true -> {
                val route = navBackStackEntry?.toRoute<IngredientEditRoute>()
                if (route?.ingredientId == IngredientEditRoute.DEFAULT_ID)
                    stringResource(R.string.title_ingredient_add)
                else
                    stringResource(R.string.title_ingredient_edit)
            }

            currentDestination?.hasRoute<RecipeEditRoute>() == true -> {
                val route = navBackStackEntry?.toRoute<RecipeEditRoute>()
                if (route?.recipeId == RecipeEditRoute.DEFAULT_ID)
                    stringResource(R.string.title_recipe_add)
                else
                    stringResource(R.string.title_recipe_edit)
            }

            currentDestination?.hasRoute<RecipeDetailRoute>() == true -> stringResource(R.string.title_recipe_detail)
            currentDestination?.hasRoute<SettingsRoute>() == true -> stringResource(R.string.title_settings)
            else -> ""
        }
    }

    // 뒤로가기 버튼 표시 여부
    val showBackButton = currentTab == null

    // 스낵바 헬퍼 함수
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

    // 뒤로가기 2번 종료
    var backPressedTime by remember { mutableLongStateOf(0L) }
    val backPressMessage = stringResource(R.string.msg_back_press_exit)

    BackHandler(enabled = currentTab == MainTab.HOME) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime > 2000L) {
            backPressedTime = currentTime
            showSnackbar(backPressMessage, SnackbarType.INFO)
        } else {
            activity?.finish()
        }
    }

    // 탭 이동 함수
    fun navigateToMainTab(tab: MainTab) {
        navController.navigate(tab.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = topBarTitle(),
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
                        if (showBackButton) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.desc_back)
                                )
                            }
                        }
                    },
                    actions = {
                        if (BuildConfig.DEBUG && currentTab == MainTab.HOME) {
                            val testMsg = stringResource(R.string.msg_notification_test)
                            IconButton(onClick = {
                                val testRequest =
                                    OneTimeWorkRequestBuilder<ExpirationCheckWorker>().build()
                                WorkManager.getInstance(context).enqueue(testRequest)
                                showSnackbar(testMsg, SnackbarType.SUCCESS)
                            }) {
                                Icon(Icons.Default.BugReport, contentDescription = stringResource(R.string.desc_notification_test))
                            }
                        }

                        if (currentTab != null) {
                            IconButton(onClick = { navController.navigate(SettingsRoute) }) {
                                Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.desc_settings))
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->

            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
                onShowSnackbar = showSnackbar,
                onShowAd = onShowAd,
                onNavigateToMainTab = { tab -> navigateToMainTab(tab) }
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
            val type = customVisuals?.type ?: SnackbarType.INFO

            val (containerColor, contentColor, icon) = when (type) {
                SnackbarType.SUCCESS -> Triple(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    Icons.Default.CheckCircle
                )

                SnackbarType.ERROR -> Triple(
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.colorScheme.onErrorContainer,
                    Icons.Default.ErrorOutline
                )

                SnackbarType.INFO -> Triple(
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.onSecondaryContainer,
                    Icons.Default.Info
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
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
}