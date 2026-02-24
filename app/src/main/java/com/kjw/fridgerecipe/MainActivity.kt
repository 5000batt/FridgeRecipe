package com.kjw.fridgerecipe

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.kjw.fridgerecipe.data.util.IngredientAnalyzer
import com.kjw.fridgerecipe.data.util.UserDictionaryManager
import com.kjw.fridgerecipe.domain.model.ThemeMode
import com.kjw.fridgerecipe.domain.repository.SettingsRepository
import com.kjw.fridgerecipe.presentation.ui.screen.MainAppScreen
import com.kjw.fridgerecipe.presentation.util.RewardedAdManager
import com.kjw.fridgerecipe.ui.theme.FridgeRecipeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var rewardedAdManager: RewardedAdManager

    @Inject
    lateinit var userDictionaryManager: UserDictionaryManager

    @Inject
    lateinit var ingredientAnalyzer: IngredientAnalyzer

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val isFirst = settingsRepository.isFirstLaunch.first()

            if (isFirst) {
                delay(150)
            }

            keepSplashScreen = false
        }

        // Remote Config 업데이트
        Firebase.remoteConfig
            .fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        userDictionaryManager.updateDictionary()
                    }
                    Log.d("MainActivity", "Remote Config updated!")
                } else {
                    Log.d("MainActivity", "Remote Config update failed")
                }
            }

        // 핵심: 코모란 Warm-up을 백그라운드 스레드에서만 수행
        // 이렇게 하면 MainActivity의 onCreate나 첫 로딩 속도에 영향을 주지 않습니다.
        lifecycleScope.launch(Dispatchers.Default) {
            ingredientAnalyzer.getIngredientNouns("로딩준비")
            Log.d("MainActivity", "Komoran warmed up in background!")
        }

        rewardedAdManager.loadAd()

        enableEdgeToEdge()
        setContent {
            val themeMode by settingsRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            val isDarkTheme =
                when (themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }

            FridgeRecipeTheme(darkTheme = isDarkTheme) {
                MainAppScreen(
                    onShowAd = { onReward ->
                        rewardedAdManager.showAd(this@MainActivity, onReward)
                    },
                )
            }
        }
    }
}
