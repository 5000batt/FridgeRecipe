package com.kjw.fridgerecipe

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.kjw.fridgerecipe.domain.model.ThemeMode
import com.kjw.fridgerecipe.domain.repository.SettingsRepository
import com.kjw.fridgerecipe.presentation.ui.screen.MainAppScreen
import com.kjw.fridgerecipe.presentation.util.RewardedAdManager
import com.kjw.fridgerecipe.ui.theme.FridgeRecipeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository
    @Inject
    lateinit var rewardedAdManager: RewardedAdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        Firebase.remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("MainActivity", "Remote Config updated!")
                } else {
                    Log.d("MainActivity", "Remote Config update failed")
                }
            }

        rewardedAdManager.loadAd()

        enableEdgeToEdge()
        setContent {
            val themeMode by settingsRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            val isDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            FridgeRecipeTheme(darkTheme = isDarkTheme) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val context = this

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = { isGranted: Boolean ->
                            lifecycleScope.launch {
                                if (isGranted) {
                                    settingsRepository.setNotificationEnabled(true)
                                } else {
                                    settingsRepository.setNotificationEnabled(false)

                                    val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    )

                                    if (shouldShowRationale) {
                                        // 알림 최초 거부 시 메세지
                                        Toast.makeText(context, "알림을 거부하여 소비기한 알림을 받을 수 없습니다.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // 알림 권한 2회 거부 시 메세지 띄우지 않도록
                                    }
                                }
                            }
                        }
                    )

                    // 앱 실행시 권한 요청 (사용자가 2회 거부시 자동으로 권한 요청 자동 거부)
                    // 앱 실행시 권한 요청하는 부분이 UX에 안좋을 수도 있으니 수정 여부 확인
                    LaunchedEffect(Unit) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                MainAppScreen(
                    onShowAd = { onReward ->
                        rewardedAdManager.showAd(this@MainActivity, onReward)
                    }
                )
            }
        }
    }
}
