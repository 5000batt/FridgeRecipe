package com.kjw.fridgerecipe.presentation.ui.components.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.presentation.util.RecipeConstants.FILTER_ANY
import kotlin.math.roundToInt

@Composable
fun TimeSliderSection(
    currentFilter: String?,
    onValueChange: (String) -> Unit,
) {
    val option1 = stringResource(R.string.option_time_15)
    val option2 = stringResource(R.string.option_time_30)
    val option3 = stringResource(R.string.option_time_60)
    val option4 = stringResource(R.string.option_time_over_60)
    val options = listOf(option1, option2, option3, option4)

    val currentIndex = options.indexOf(currentFilter).takeIf { it >= 0 } ?: -1
    val sliderValue = if (currentIndex == -1) 0f else (currentIndex + 1).toFloat()

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.home_filter_time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = currentFilter ?: stringResource(R.string.filter_any),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = sliderValue,
            onValueChange = { value ->
                val index = value.roundToInt()
                if (index == 0) {
                    onValueChange(FILTER_ANY)
                } else {
                    onValueChange(options[index - 1])
                }
            },
            valueRange = 0f..4f,
            steps = 3,
            colors =
                SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
        )
    }
}
