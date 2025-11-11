package com.kjw.fridgerecipe.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kjw.fridgerecipe.domain.model.Ingredient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun IngredientChip(
    ingredient: Ingredient,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val chipColor = when {
        ingredient.expirationDate.isBefore(LocalDate.now()) -> Color.Red.copy(alpha = 0.4F)
        ingredient.expirationDate.isBefore(LocalDate.now().plusDays(3)) -> Color.Yellow.copy(alpha = 0.4F)
        else -> Color.LightGray
    }

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 100.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onClick
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = chipColor),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IngredientIconImage(ingredient)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = ingredient.name,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = ingredient.expirationDate.format(DateTimeFormatter.ofPattern("y.MM.dd")),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun IngredientIconImage(ingredient: Ingredient) {
    val imagePainter = painterResource(id = ingredient.emoticon.iconResId)

    Image(
        painter = imagePainter,
        contentDescription = ingredient.emoticon.label + " 아이콘",
        modifier = Modifier.size(48.dp),
        contentScale = ContentScale.Fit
    )
}