package com.kjw.fridgerecipe.presentation.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun FadeScrollLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    gradientHeight: Dp = 24.dp,
    content: LazyListScope.() -> Unit,
) {
    val density = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val topGradientAlpha by remember(state, density, gradientHeight) {
        derivedStateOf {
            val firstItemVisible = state.firstVisibleItemIndex == 0
            val scrollOffset = state.firstVisibleItemScrollOffset.toFloat()
            val heightPx = with(density) { gradientHeight.toPx() }

            if (!firstItemVisible) {
                1f
            } else {
                (scrollOffset / heightPx).coerceIn(0f, 1f)
            }
        }
    }

    LaunchedEffect(state.isScrollInProgress) {
        if (state.isScrollInProgress) {
            keyboardController?.hide()
        }
    }

    Box(modifier = modifier) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            content = content,
        )

        val backgroundColor = MaterialTheme.colorScheme.background
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(gradientHeight)
                    .align(Alignment.TopCenter)
                    .zIndex(1f)
                    .alpha(topGradientAlpha)
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    backgroundColor,
                                    backgroundColor.copy(alpha = 0.8f),
                                    Color.Transparent,
                                ),
                        ),
                    ),
        )
    }
}
