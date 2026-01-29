package com.kjw.fridgerecipe.presentation.ui.screen.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.presentation.ui.components.common.BottomActionBar
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonTopBar
import com.kjw.fridgerecipe.presentation.ui.components.common.FridgeBottomButton
import com.kjw.fridgerecipe.presentation.ui.components.common.LoadingContent
import com.kjw.fridgerecipe.presentation.ui.components.recipe.detail.IngredientListItem
import com.kjw.fridgerecipe.presentation.ui.components.recipe.detail.RecipeInfoRow
import com.kjw.fridgerecipe.presentation.ui.components.recipe.detail.RecipeStepItem
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeDetailViewModel

@Composable
fun RecipeDetailScreen(
    onNavigateToRecipeEdit: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel(),
    recipeId: Long
) {
    val recipe by viewModel.recipe.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearRecipe()
        }
    }

    @Composable
    fun getCategoryLabel(type: RecipeCategoryType?): String {
        return type?.let { stringResource(it.labelResId) }
            ?: stringResource(R.string.recipe_detail_default_category)
    }

    LoadingContent(isLoading = isLoading) {
        recipe?.let { matchData ->
            val currentRecipe = matchData.recipe

            Scaffold(
                topBar = {
                    CommonTopBar(
                        title = stringResource(R.string.title_recipe_detail),
                        onNavigateBack = onNavigateBack
                    )
                },
                bottomBar = {
                    BottomActionBar {
                        FridgeBottomButton(
                            text = stringResource(R.string.recipe_detail_btn_edit),
                            onClick = { currentRecipe.id?.let { onNavigateToRecipeEdit(it) } },
                            modifier = Modifier.fillMaxWidth(),
                            icon = { Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.recipe_detail_btn_edit)) }
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.background,
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
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
                                if (currentRecipe.imageUri != null) {
                                    AsyncImage(
                                        model = currentRecipe.imageUri,
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
                                val displayCategory = getCategoryLabel(currentRecipe.searchMetadata?.categoryFilter)

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = displayCategory,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Surface(
                                        color = if (matchData.isCookable) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.recipe_match_rate_format, matchData.matchPercentage),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (matchData.isCookable) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = currentRecipe.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                RecipeInfoRow(recipe = currentRecipe)

                                Spacer(modifier = Modifier.height(32.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(32.dp))

                                Text(
                                    text = stringResource(R.string.recipe_detail_ingredients_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                            }
                        }

                        items(currentRecipe.ingredients) { ingredient ->
                            val isMissing = matchData.missingIngredients.any { missingName ->
                                ingredient.name.contains(missingName)
                            }

                            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                                IngredientListItem(
                                    ingredient = ingredient,
                                    isMissing = isMissing
                                )
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
                            }
                        }

                        itemsIndexed(currentRecipe.steps) { index, step ->
                            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                                RecipeStepItem(index + 1, step)
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}