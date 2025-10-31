package com.kjw.fridgerecipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kjw.fridgerecipe.presentation.ui.screen.MainAppScreen
import com.kjw.fridgerecipe.ui.theme.FridgeRecipeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FridgeRecipeTheme {
                MainAppScreen()
            }
        }
    }
}