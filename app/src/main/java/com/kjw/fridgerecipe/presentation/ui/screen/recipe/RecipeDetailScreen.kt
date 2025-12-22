package com.kjw.fridgerecipe.presentation.ui.screen.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.presentation.ui.components.recipe.IngredientListItem
import com.kjw.fridgerecipe.presentation.ui.components.recipe.RecipeInfoRow
import com.kjw.fridgerecipe.presentation.ui.components.recipe.RecipeStepItem
import com.kjw.fridgerecipe.presentation.util.RecipeConstants
import com.kjw.fridgerecipe.presentation.util.RecipeConstants.FILTER_ANY
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeDetailViewModel

@Composable
fun RecipeDetailScreen(
    onNavigateToRecipeEdit: (Long) -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel(),
    recipeId: Long
) {
    val recipe by viewModel.recipe.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearRecipe()
        }
    }

    fun getCategoryLabel(value: String): String {
        return RecipeConstants.CATEGORY_FILTER_OPTIONS
            .find { it.value == value }?.label?.asString(context)
            ?: value
    }

    if (recipe == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val recipe = recipe!!

        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (recipe.imageUri != null) {
                            AsyncImage(
                                model = recipe.imageUri,
                                contentDescription = stringResource(R.string.recipe_detail_image_desc),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SoupKitchen,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        val categoryFilter = recipe.searchMetadata?.categoryFilter
                        val displayCategory = if (categoryFilter == FILTER_ANY || categoryFilter.isNullOrBlank()) {
                            stringResource(R.string.recipe_detail_default_category)
                        } else {
                            getCategoryLabel(categoryFilter)
                        }

                        Text(
                            text = displayCategory,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = recipe.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        RecipeInfoRow(recipe = recipe)

                        Spacer(modifier = Modifier.height(32.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = stringResource(R.string.recipe_detail_ingredients_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                items(recipe.ingredients) { ingredient ->
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        IngredientListItem(ingredient)
                    }
                }

                item {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = stringResource(R.string.recipe_detail_steps_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                itemsIndexed(recipe.steps) { index, step ->
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        RecipeStepItem(index + 1, step)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 16.dp,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { recipe.id?.let { onNavigateToRecipeEdit(it) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = stringResource(R.string.recipe_detail_btn_edit),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}