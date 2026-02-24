package com.kjw.fridgerecipe.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme =
    darkColorScheme(
        primary = GreenPrimaryDark,
        onPrimary = GreenOnPrimaryDark,
        primaryContainer = GreenPrimaryContainerDark,
        onPrimaryContainer = GreenOnPrimaryContainerDark,
        secondary = LimeSecondaryDark,
        onSecondary = LimeOnSecondaryDark,
        secondaryContainer = LimeSecondaryContainerDark,
        onSecondaryContainer = LimeOnSecondaryContainerDark,
        background = BackgroundDark,
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
        surfaceVariant = GreenSurfaceVariantDark,
        onSurfaceVariant = GreenOnSurfaceVariantDark,
        outline = GreenOutlineDark,
        error = StatusExpiredContentDark,
        onError = StatusExpiredContainerDark,
        errorContainer = StatusExpiredContainerDark,
        onErrorContainer = StatusExpiredContentDark,
        tertiary = StatusWarningContentDark,
        onTertiary = StatusWarningContainerDark,
        tertiaryContainer = StatusWarningContainerDark,
        onTertiaryContainer = StatusWarningContentDark,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = GreenPrimary,
        onPrimary = GreenOnPrimary,
        primaryContainer = GreenPrimaryContainer,
        onPrimaryContainer = GreenOnPrimaryContainer,
        secondary = LimeSecondary,
        onSecondary = LimeOnSecondary,
        secondaryContainer = LimeSecondaryContainer,
        onSecondaryContainer = LimeOnSecondaryContainer,
        background = BackgroundLight,
        surface = SurfaceLight,
        onSurface = OnSurfaceLight,
        surfaceVariant = GreenSurfaceVariantLight,
        onSurfaceVariant = GreenOnSurfaceVariantLight,
        outline = GreenOutlineLight,
        error = StatusExpiredContentLight,
        onError = StatusExpiredContainerLight,
        errorContainer = StatusExpiredContainerLight,
        onErrorContainer = StatusExpiredContentLight,
        tertiary = StatusWarningContentLight,
        onTertiary = StatusWarningContainerLight,
        tertiaryContainer = StatusWarningContainerLight,
        onTertiaryContainer = StatusWarningContentLight,
    )

@Composable
fun FridgeRecipeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
