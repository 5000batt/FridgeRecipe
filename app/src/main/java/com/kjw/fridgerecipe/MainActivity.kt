package com.kjw.fridgerecipe

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kjw.fridgerecipe.presentation.ui.screen.MainAppScreen
import com.kjw.fridgerecipe.presentation.viewmodel.IngredientViewModel
import com.kjw.fridgerecipe.ui.theme.FridgeRecipeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: IngredientViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.isLoading.value
            }
        }

        enableEdgeToEdge()
        setContent {
            FridgeRecipeTheme {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = { isGranted: Boolean ->
                            if (isGranted) {

                            } else {
                                Toast.makeText(this, "알림을 거부하여 소비기한 알림을 받을 수 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    LaunchedEffect(Unit) {
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                MainAppScreen()
            }
        }
    }
}