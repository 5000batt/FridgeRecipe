package com.kjw.fridgerecipe.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep

@Composable
fun RecipeInfoRow(recipe: Recipe) {
    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        InfoItem(icon = Icons.Default.Person, text = recipe.servings)
        InfoItem(icon = Icons.Default.Favorite, text = recipe.time)
        InfoItem(icon = Icons.Default.Star, text = recipe.level.label)
    }
}

@Composable
fun InfoItem(icon: ImageVector, text: String) {
    Column (horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun IngredientListItem(ingredient: RecipeIngredient) {
    Row (
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(ingredient.name, style = MaterialTheme.typography.bodyLarge)
        Text(ingredient.quantity, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun RecipeStepItem(step: RecipeStep) {
    Row (
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "${step.number}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "${step.description}",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}