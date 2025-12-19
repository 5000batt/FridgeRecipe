package com.kjw.fridgerecipe.presentation.ui.screen.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.presentation.ui.components.ingredient.StorageSection
import com.kjw.fridgerecipe.presentation.ui.model.ListDisplayType
import com.kjw.fridgerecipe.presentation.viewmodel.FILTER_ANY
import com.kjw.fridgerecipe.presentation.viewmodel.IngredientViewModel
import com.kjw.fridgerecipe.presentation.viewmodel.RecipeViewModel
import com.kjw.fridgerecipe.presentation.ui.components.common.IngredientStatusLegend
import com.kjw.fridgerecipe.presentation.ui.components.home.FilterSection
import com.kjw.fridgerecipe.presentation.ui.components.home.RecipeLoadingScreen
import com.kjw.fridgerecipe.presentation.ui.components.home.TimeSliderSection
import com.kjw.fridgerecipe.presentation.util.SnackbarType
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    ingredientViewModel: IngredientViewModel = hiltViewModel(),
    recipeViewModel: RecipeViewModel = hiltViewModel(),
    onNavigateToRecipeDetail: (Long) -> Unit,
    onNavigateToIngredientAdd: () -> Unit,
    onShowAd: (onReward: () -> Unit) -> Unit,
    onShowSnackbar: (String, SnackbarType) -> Unit
) {
    val uiState by recipeViewModel.homeUiState.collectAsState()

    val homeIngredients by ingredientViewModel.homeScreenIngredients.collectAsState()
    val remainingTickets by recipeViewModel.remainingTickets.collectAsState()

    val currentTickets by rememberUpdatedState(remainingTickets)

    val levelFilterOptions = RecipeViewModel.LEVEL_FILTER_OPTIONS
    val categoryFilterOptions = RecipeViewModel.CATEGORY_FILTER_OPTIONS
    val utensilFilterOptions = RecipeViewModel.UTENSIL_FILTER_OPTIONS

    LaunchedEffect(Unit) {
        if (uiState.isRecipeLoading && uiState.recommendedRecipe != null) {
            recipeViewModel.resetHomeState()
        }

        recipeViewModel.sideEffect.collect { event ->
            when (event) {
                is RecipeViewModel.HomeSideEffect.NavigateToRecipeDetail -> {
                    onNavigateToRecipeDetail(event.recipeId)
                    delay(400)
                    recipeViewModel.resetHomeState()
                }
                is RecipeViewModel.HomeSideEffect.ShowSnackbar -> {
                    onShowSnackbar(event.message, SnackbarType.SUCCESS)
                }
            }
        }
    }

    val context = LocalContext.current
    DisposableEffect(context) {
        val timeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_DATE_CHANGED ||
                    intent?.action == Intent.ACTION_TIME_CHANGED) {
                    if (currentTickets < 3) {
                        recipeViewModel.checkTicketReset()
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
        }
        context.registerReceiver(timeReceiver, filter)

        onDispose {
            context.unregisterReceiver(timeReceiver)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Text(
                    text = stringResource(R.string.home_title_fridge),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IngredientStatusLegend(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            items(StorageType.entries) { storageType ->
                val items = homeIngredients[storageType] ?: emptyList()

                if (items.isNotEmpty()) {
                    StorageSection(
                        title = storageType.label,
                        items = items,
                        displayType = ListDisplayType.ROW,
                        modifier = Modifier.padding(vertical = 8.dp),
                        selectedIngredientIds = uiState.selectedIngredientIds,
                        onIngredientClick = { ingredient ->
                            ingredient.id?.let { recipeViewModel.toggleIngredientSelection(it) }
                        }
                    )
                }
            }

            if (homeIngredients.values.all { it.isEmpty() }) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .clickable { onNavigateToIngredientAdd() }
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.home_empty_title), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.home_empty_desc), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            item {
                Text(
                    text = stringResource(R.string.home_filter_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        TimeSliderSection(
                            currentFilter = uiState.filterState.timeLimit,
                            onValueChange = { recipeViewModel.onTimeFilterChanged(it) }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        FilterSection(
                            title = stringResource(R.string.home_filter_level),
                            options = levelFilterOptions.map { it?.label ?: FILTER_ANY },
                            selectedOption = uiState.filterState.level?.label ?: FILTER_ANY,
                            onOptionSelected = { label ->
                                val level = levelFilterOptions.find { (it?.label ?: FILTER_ANY) == label }
                                recipeViewModel.onLevelFilterChanged(level)
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        FilterSection(
                            title = stringResource(R.string.home_filter_category),
                            options = categoryFilterOptions,
                            selectedOption = uiState.filterState.category ?: FILTER_ANY,
                            onOptionSelected = { recipeViewModel.onCategoryFilterChanged(it) }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        FilterSection(
                            title = stringResource(R.string.home_filter_utensil),
                            options = utensilFilterOptions,
                            selectedOption = uiState.filterState.utensil ?: FILTER_ANY,
                            onOptionSelected = { recipeViewModel.onUtensilFilterChanged(it) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.home_filter_only_selected_title),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = stringResource(R.string.home_filter_only_selected_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.filterState.useOnlySelected,
                                onCheckedChange = { recipeViewModel.onUseOnlySelectedIngredientsChanged(it) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
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
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val buttonText = when {
                    uiState.isRecipeLoading -> stringResource(R.string.home_btn_loading)
                    uiState.selectedIngredientIds.isEmpty() -> stringResource(R.string.home_btn_select_ingredient)
                    uiState.recommendedRecipe == null -> stringResource(R.string.home_btn_recommend)
                    else -> stringResource(R.string.home_btn_recommend_another)
                }

                Text(
                    text = stringResource(R.string.ticket_count_format, remainingTickets, 3),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (remainingTickets > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Button(
                    onClick = {
                        val allIngredients = ingredientViewModel.allIngredients.value
                        val selectedIngredients =
                            allIngredients.filter { it.id in uiState.selectedIngredientIds }
                        recipeViewModel.checkIngredientConflicts(selectedIngredients)
                    },
                    enabled = uiState.selectedIngredientIds.isNotEmpty() && !uiState.isRecipeLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (uiState.isRecipeLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (uiState.isRecipeLoading) {
            Dialog(
                onDismissRequest = { },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false,
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                RecipeLoadingScreen()
            }
        }
    }

    if (uiState.showConflictDialog) {
        val conflictNames = uiState.conflictIngredients.joinToString(", ")

        AlertDialog(
            onDismissRequest = { recipeViewModel.dismissConflictDialog() },
            title = { Text(text = stringResource(R.string.home_dialog_conflict_title)) },
            text = {
                Text(
                    text = stringResource(R.string.home_dialog_conflict_msg, conflictNames),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val allIngredients = ingredientViewModel.allIngredients.value
                        val selectedIngredients = allIngredients.filter { it.id in uiState.selectedIngredientIds }
                        recipeViewModel.fetchRecommendedRecipe(selectedIngredients)
                    }
                ) {
                    Text(stringResource(R.string.home_dialog_conflict_btn_yes), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recipeViewModel.dismissConflictDialog() }) {
                    Text(stringResource(R.string.btn_no))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    uiState.errorDialogState?.let { errorState ->
        AlertDialog(
            onDismissRequest = { recipeViewModel.dismissErrorDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(errorState.title)
                }
            },
            text = {
                Text(
                    text = errorState.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { recipeViewModel.dismissErrorDialog() }) {
                    Text(stringResource(R.string.btn_confirm))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (uiState.showAdDialog) {
        AlertDialog(
            onDismissRequest = { recipeViewModel.dismissAdDialog() },
            title = { Text(text = stringResource(R.string.ticket_dialog_empty_title)) },
            text = {
                Text(
                    text = stringResource(R.string.ticket_dialog_empty_msg),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onShowAd {
                            recipeViewModel.onAdWatched()
                        }
                    }
                ) {
                    Text(stringResource(R.string.ticket_dialog_btn_charge), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recipeViewModel.dismissAdDialog() }) {
                    Text(stringResource(R.string.ticket_dialog_btn_next_time))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}