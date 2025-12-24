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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.kjw.fridgerecipe.presentation.navigation.MainTab
import com.kjw.fridgerecipe.presentation.ui.components.common.AppBottomNavigationBar
import com.kjw.fridgerecipe.presentation.ui.components.ingredient.StorageSection
import com.kjw.fridgerecipe.presentation.ui.model.ListDisplayType
import com.kjw.fridgerecipe.presentation.ui.components.common.IngredientStatusLegend
import com.kjw.fridgerecipe.presentation.ui.components.common.LoadingContent
import com.kjw.fridgerecipe.presentation.ui.components.common.ConfirmDialog
import com.kjw.fridgerecipe.presentation.ui.components.common.ErrorDialog
import com.kjw.fridgerecipe.presentation.ui.components.common.FridgeBottomButton
import com.kjw.fridgerecipe.presentation.ui.components.home.FilterSection
import com.kjw.fridgerecipe.presentation.ui.components.home.RecipeLoadingScreen
import com.kjw.fridgerecipe.presentation.ui.components.home.TimeSliderSection
import com.kjw.fridgerecipe.presentation.util.RecipeConstants
import com.kjw.fridgerecipe.presentation.util.SnackbarType
import com.kjw.fridgerecipe.presentation.viewmodel.HomeViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    onNavigateToRecipeDetail: (Long) -> Unit,
    onNavigateToIngredientEdit: () -> Unit,
    onShowAd: (onReward: () -> Unit) -> Unit,
    onShowSnackbar: (String, SnackbarType) -> Unit,
    onNavigateToMainTab: (MainTab) -> Unit
) {
    val uiState by homeViewModel.homeUiState.collectAsState()
    val context = LocalContext.current

    val levelFilterOptions = RecipeConstants.LEVEL_FILTER_OPTIONS
    val categoryFilterOptions = RecipeConstants.CATEGORY_FILTER_OPTIONS
    val utensilFilterOptions = RecipeConstants.UTENSIL_FILTER_OPTIONS

    LaunchedEffect(Unit) {
        if (uiState.isRecipeLoading && uiState.recommendedRecipe != null) {
            homeViewModel.resetHomeState()
        }

        homeViewModel.sideEffect.collect { event ->
            when (event) {
                is HomeViewModel.HomeSideEffect.NavigateToRecipeDetail -> {
                    onNavigateToRecipeDetail(event.recipeId)
                    delay(400)
                    homeViewModel.resetHomeState()
                }
                is HomeViewModel.HomeSideEffect.ShowSnackbar -> {
                    onShowSnackbar(event.message.asString(context), SnackbarType.SUCCESS)
                }
            }
        }
    }

    // 시간 변화 감지 (티켓 리셋용)
    DisposableEffect(context) {
        val timeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_DATE_CHANGED ||
                    intent?.action == Intent.ACTION_TIME_CHANGED) {
                    if (uiState.remainingTickets < 3) {
                        homeViewModel.checkTicketReset()
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
        }
        context.registerReceiver(timeReceiver, filter)
        onDispose { context.unregisterReceiver(timeReceiver) }
    }

    LoadingContent(isLoading = uiState.isIngredientLoading) {
        Scaffold(
            bottomBar = {
                AppBottomNavigationBar(
                    currentTab = MainTab.HOME,
                    onTabSelected = onNavigateToMainTab
                )
            },
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.statusBars),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
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
                        val items = uiState.storageIngredients[storageType] ?: emptyList()

                        if (items.isNotEmpty()) {
                            StorageSection(
                                title = storageType.label,
                                items = items,
                                displayType = ListDisplayType.ROW,
                                modifier = Modifier.padding(vertical = 8.dp),
                                selectedIngredientIds = uiState.selectedIngredientIds,
                                onIngredientClick = { ingredient ->
                                    ingredient.id?.let { homeViewModel.toggleIngredientSelection(it) }
                                }
                            )
                        }
                    }

                    if (uiState.storageIngredients.values.all { it.isEmpty() }) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                                    .clickable { onNavigateToIngredientEdit() }
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
                                    onValueChange = { homeViewModel.onTimeFilterChanged(it) }
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                val selectedLevelOption = levelFilterOptions.find { it.value == uiState.filterState.level }

                                FilterSection(
                                    title = stringResource(R.string.home_filter_level),
                                    options = levelFilterOptions,
                                    selectedOption = selectedLevelOption,
                                    onOptionSelected = { option ->
                                        homeViewModel.onLevelFilterChanged(option.value)
                                    },
                                    itemLabel = { it.label.asString(context) }
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                val selectedCategoryOption = categoryFilterOptions.find { it.value == uiState.filterState.category }
                                    ?: categoryFilterOptions.first()

                                FilterSection(
                                    title = stringResource(R.string.home_filter_category),
                                    options = categoryFilterOptions,
                                    selectedOption = selectedCategoryOption,
                                    onOptionSelected = { option ->
                                        homeViewModel.onCategoryFilterChanged(option.value)
                                    },
                                    itemLabel = { it.label.asString(context) }
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                val selectedUtensilOption = utensilFilterOptions.find { it.value == uiState.filterState.utensil }
                                    ?: utensilFilterOptions.first()

                                FilterSection(
                                    title = stringResource(R.string.home_filter_utensil),
                                    options = utensilFilterOptions,
                                    selectedOption = selectedUtensilOption,
                                    onOptionSelected = { option ->
                                        homeViewModel.onUtensilFilterChanged(option.value)
                                    },
                                    itemLabel = { it.label.asString(context) }
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
                                        onCheckedChange = { homeViewModel.onUseOnlySelectedIngredientsChanged(it) }
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
                            text = stringResource(R.string.ticket_count_format, uiState.remainingTickets, 3),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (uiState.remainingTickets > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        FridgeBottomButton(
                            text = buttonText,
                            onClick = { homeViewModel.checkIngredientConflicts() },
                            isEnabled = uiState.selectedIngredientIds.isNotEmpty() && !uiState.isRecipeLoading,
                            isLoading = uiState.isRecipeLoading,
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            icon = {
                                if (!uiState.isRecipeLoading) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                }
                            }
                        )
                    }
                }
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

    if (uiState.showConflictDialog) {
        val conflictNames = uiState.conflictIngredients.joinToString(", ")

        ConfirmDialog(
            title = stringResource(R.string.home_dialog_conflict_title),
            message = stringResource(R.string.home_dialog_conflict_msg, conflictNames),
            confirmText = stringResource(R.string.home_dialog_conflict_btn_yes),
            dismissText = stringResource(R.string.btn_no),
            confirmColor = MaterialTheme.colorScheme.primary,
            onConfirm = { homeViewModel.onConfirmConflict() },
            onDismiss = { homeViewModel.dismissConflictDialog() }
        )
    }

    uiState.errorDialogState?.let { errorState ->
        ErrorDialog(
            title = errorState.title.asString(context),
            message = errorState.message.asString(context),
            onDismiss = { homeViewModel.dismissErrorDialog() }
        )
    }

    if (uiState.showAdDialog) {
        ConfirmDialog(
            title = stringResource(R.string.ticket_dialog_empty_title),
            message = stringResource(R.string.ticket_dialog_empty_msg),
            confirmText = stringResource(R.string.ticket_dialog_btn_charge),
            dismissText = stringResource(R.string.ticket_dialog_btn_next_time),
            confirmColor = MaterialTheme.colorScheme.primary,
            onConfirm = { onShowAd { homeViewModel.onAdWatched() } },
            onDismiss = { homeViewModel.dismissAdDialog() }
        )
    }
}