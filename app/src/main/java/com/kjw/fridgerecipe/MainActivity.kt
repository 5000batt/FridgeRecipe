package com.kjw.fridgerecipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
                MainAppScreen()
            }
        }
    }
}