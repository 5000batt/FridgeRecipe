package com.kjw.fridgerecipe.presentation.ui.components.common

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun CommonFloatingActionButton(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val isExpanded by remember(state) {
        derivedStateOf {
            !state.canScrollBackward || !state.canScrollForward
        }
    }

    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(imageVector = icon, contentDescription = null) },
        text = { Text(text = text) },
        expanded = isExpanded,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier,
    )
}
