package com.kjw.fridgerecipe.presentation.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun IngredientStatusLegend(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        StatusIndicator(
            color = MaterialTheme.colorScheme.primaryContainer,
            text = "여유"
        )

        Spacer(modifier = Modifier.width(8.dp))

        StatusIndicator(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            text = "임박"
        )

        Spacer(modifier = Modifier.width(8.dp))

        StatusIndicator(
            color = MaterialTheme.colorScheme.errorContainer,
            text = "만료"
        )
    }
}

@Composable
private fun StatusIndicator(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}