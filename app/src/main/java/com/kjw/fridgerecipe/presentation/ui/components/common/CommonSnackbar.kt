package com.kjw.fridgerecipe.presentation.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kjw.fridgerecipe.presentation.util.CustomSnackbarVisuals
import com.kjw.fridgerecipe.presentation.util.SnackbarType

@Composable
fun CommonSnackbar(snackbarData: SnackbarData) {
    val customVisuals = snackbarData.visuals as? CustomSnackbarVisuals
    val type = customVisuals?.type ?: SnackbarType.INFO

    val (containerColor, contentColor, icon) =
        when (type) {
            SnackbarType.SUCCESS ->
                Triple(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    Icons.Default.CheckCircle,
                )
            SnackbarType.ERROR ->
                Triple(
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.colorScheme.onErrorContainer,
                    Icons.Default.ErrorOutline,
                )
            SnackbarType.INFO ->
                Triple(
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.onSecondaryContainer,
                    Icons.Default.Info,
                )
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Snackbar(
            modifier =
                Modifier
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 420.dp),
            containerColor = containerColor,
            contentColor = contentColor,
            shape = RoundedCornerShape(8.dp),
            action = {
                snackbarData.visuals.actionLabel?.let { actionLabel ->
                    TextButton(
                        onClick = { snackbarData.dismiss() },
                    ) {
                        Text(
                            text = actionLabel,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 4.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = snackbarData.visuals.message,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
