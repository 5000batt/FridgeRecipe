package com.kjw.fridgerecipe.presentation.ui.components.recipe.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kjw.fridgerecipe.domain.model.Recipe

@Composable
fun RecipeListItem(
    recipe: Recipe,
    onRecipeClick: () -> Unit
) {
    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onRecipeClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            if (recipe.imageUri != null) {
                AsyncImage(
                    model = recipe.imageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Companion.Crop,
                    modifier = Modifier.Companion
                        .size(100.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            } else {
                Box(
                    modifier = Modifier.Companion
                        .size(100.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    Icon(
                        Icons.Default.SoupKitchen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.Companion.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.Companion.width(16.dp))

            Column(
                modifier = Modifier.Companion
                    .weight(1f)
                    .height(100.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Companion.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Companion.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.Companion.padding(top = 4.dp)
                )

                Row(
                    verticalAlignment = Alignment.Companion.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    RecipeInfoChip(icon = Icons.Default.AccessTime, text = recipe.time)

                    Spacer(modifier = Modifier.Companion.width(8.dp))

                    Box(
                        modifier = Modifier.Companion
                            .size(2.dp)
                            .background(MaterialTheme.colorScheme.outline, CircleShape)
                    )

                    Spacer(modifier = Modifier.Companion.width(8.dp))

                    RecipeInfoChip(
                        icon = Icons.Default.SignalCellularAlt,
                        text = recipe.level.label
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeInfoChip(
    icon: ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.Companion.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.Companion.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.Companion.width(4.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}