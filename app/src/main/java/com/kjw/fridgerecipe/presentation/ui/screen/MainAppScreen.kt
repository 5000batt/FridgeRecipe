package com.kjw.fridgerecipe.presentation.ui.screen

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.presentation.navigation.AppNavHost
import com.kjw.fridgerecipe.presentation.navigation.MainTab
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonSnackbar
import com.kjw.fridgerecipe.presentation.util.CustomSnackbarVisuals
import com.kjw.fridgerecipe.presentation.util.SnackbarType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(onShowAd: (onReward: () -> Unit) -> Unit) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = (context as? Activity)

    // 현재 화면 상태
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 현재 탭 확인
    val currentTab = MainTab.entries.find { it.isSelected(currentDestination) }

    // 스낵바 헬퍼 함수
    val closeLabel = stringResource(R.string.msg_closed)
    val showSnackbar: (String, SnackbarType) -> Unit = { message, type ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val job =
                launch {
                    snackbarHostState.showSnackbar(
                        CustomSnackbarVisuals(
                            message = message,
                            type = type,
                            duration = SnackbarDuration.Indefinite,
                            actionLabel = closeLabel,
                        ),
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
        AppNavHost(
            navController = navController,
            modifier = Modifier.fillMaxSize(),
            onShowSnackbar = showSnackbar,
            onShowAd = onShowAd,
            onNavigateToMainTab = { tab -> navigateToMainTab(tab) },
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
                    .systemBarsPadding(),
        ) { data ->
            CommonSnackbar(snackbarData = data)
        }
    }
}
