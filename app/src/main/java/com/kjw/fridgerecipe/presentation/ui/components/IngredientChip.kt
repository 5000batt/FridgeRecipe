package com.kjw.fridgerecipe.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun IngredientChip(ingredient: Ingredient) {
    val chipColor = when {
        ingredient.expirationDate.isBefore(LocalDate.now()) -> Color.Red.copy(alpha = 0.4F)
        ingredient.expirationDate.isBefore(LocalDate.now().plusDays(3)) -> Color.Yellow.copy(alpha = 0.4F)
        else -> Color.LightGray
    }

    Column(
        modifier = Modifier
            .size(width = 100.dp, height = 100.dp)
            .background(chipColor, RoundedCornerShape(8.dp))
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

@Composable
fun IngredientIconImage(ingredient: Ingredient) {
    val imagePainter = painterResource(id = ingredient.emoticon.iconResId)

    Image(
        painter = imagePainter,
        contentDescription = ingredient.emoticon.description + " 아이콘",
        modifier = Modifier.size(48.dp),
        contentScale = ContentScale.Fit
    )
}