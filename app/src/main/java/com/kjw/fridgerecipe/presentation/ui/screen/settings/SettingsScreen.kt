package com.kjw.fridgerecipe.presentation.ui.screen.settings

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.kjw.fridgerecipe.BuildConfig
import com.kjw.fridgerecipe.presentation.viewmodel.SettingsViewModel
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.presentation.ui.components.settings.SettingsClickableItem
import com.kjw.fridgerecipe.presentation.ui.components.settings.SettingsInfoItem
import com.kjw.fridgerecipe.presentation.ui.components.settings.SettingsSectionTitle
import com.kjw.fridgerecipe.presentation.ui.components.settings.SettingsSwitchItem
import com.kjw.fridgerecipe.presentation.ui.components.settings.ThemeOptionChip
import com.kjw.fridgerecipe.presentation.util.SnackbarType

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onShowSnackbar: (String, SnackbarType) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 알림 권한 상태 동기화
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val isGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    viewModel.syncNotificationState(isGranted)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionTitle(stringResource(R.string.settings_section_notification))
            SettingsSwitchItem(
                title = stringResource(R.string.settings_item_push_notification),
                description = stringResource(R.string.settings_desc_push_notification),
                checked = uiState.isNotificationEnabled,
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val isGranted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED

                            if (isGranted) viewModel.toggleNotification(true)
                            else viewModel.showPermissionDialog()
                        } else {
                            viewModel.toggleNotification(true)
                        }
                    } else {
                        viewModel.toggleNotification(false)
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            SettingsSectionTitle(stringResource(R.string.settings_section_general))
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = stringResource(R.string.settings_item_theme),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeOptionChip(
                        text = stringResource(R.string.settings_theme_system),
                        selected = uiState.isDarkMode == null,
                        onClick = { viewModel.setTheme(null) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionChip(
                        text = stringResource(R.string.settings_theme_light),
                        selected = uiState.isDarkMode == false,
                        onClick = { viewModel.setTheme(false) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionChip(
                        text = stringResource(R.string.settings_theme_dark),
                        selected = uiState.isDarkMode == true,
                        onClick = { viewModel.setTheme(true) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(text = stringResource(R.string.settings_item_excluded), style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.settings_desc_excluded),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = uiState.newExcludedIngredient,
                        onValueChange = { viewModel.onNewExcludedIngredientChanged(it) },
                        placeholder = { Text(stringResource(R.string.settings_hint_excluded)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.addExcludedIngredient() },
                        shape = RoundedCornerShape(12.dp),
                        enabled = uiState.newExcludedIngredient.isNotBlank()
                    ) {
                        Text(stringResource(R.string.settings_btn_add))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.excludedIngredients.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uiState.excludedIngredients.forEach { item ->
                            InputChip(
                                selected = false,
                                onClick = { },
                                label = { Text(item) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.settings_desc_remove_excluded, item),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { viewModel.removeExcludedIngredient(item) }
                                    )
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = null
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            SettingsSectionTitle(stringResource(R.string.settings_section_data))
            SettingsClickableItem(
                title = stringResource(R.string.settings_item_reset_ingredients),
                description = stringResource(R.string.settings_desc_reset_ingredients),
                isDestructive = true,
                onClick = { viewModel.showResetIngredientsDialog() }
            )

            SettingsClickableItem(
                title = stringResource(R.string.settings_item_reset_recipes),
                description = stringResource(R.string.settings_desc_reset_recipes),
                isDestructive = true,
                onClick = { viewModel.showResetRecipesDialog() }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            SettingsSectionTitle(stringResource(R.string.settings_section_info))
            SettingsInfoItem(
                title = stringResource(R.string.settings_item_version),
                value = "v${BuildConfig.VERSION_NAME}"
            )
            SettingsClickableItem(
                title = stringResource(R.string.settings_item_contact),
                onClick = { sendEmail(context, onShowSnackbar) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (uiState.showResetIngredientsDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissResetIngredientsDialog() },
            title = { Text(stringResource(R.string.settings_dialog_reset_ingredients_title)) },
            text = { Text(stringResource(R.string.settings_dialog_reset_ingredients_msg)) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetIngredients() }) {
                    Text(stringResource(R.string.settings_btn_delete_data), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissResetIngredientsDialog() }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    if (uiState.showResetRecipesDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissResetRecipesDialog() },
            title = { Text(stringResource(R.string.settings_dialog_reset_recipes_title)) },
            text = { Text(stringResource(R.string.settings_dialog_reset_recipes_msg)) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetRecipes() }) {
                    Text(stringResource(R.string.settings_btn_delete_data), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissResetRecipesDialog() }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    if (uiState.showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPermissionDialog() },
            title = { Text(stringResource(R.string.settings_dialog_permission_title)) },
            text = { Text(stringResource(R.string.settings_dialog_permission_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissPermissionDialog()
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text(stringResource(R.string.settings_btn_go_to_settings), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissPermissionDialog() }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

private fun sendEmail(context: Context, onShowSnackbar: (String, SnackbarType) -> Unit) {
    val subject = context.getString(R.string.settings_email_subject)
    val body = context.getString(
        R.string.settings_email_body_format,
        BuildConfig.VERSION_NAME,
        Build.MODEL,
        Build.VERSION.SDK_INT
    )

    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf("5000batt@gmail.com"))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    try {
        context.startActivity(emailIntent)
    } catch (e: ActivityNotFoundException) {
        onShowSnackbar(
            context.getString(R.string.settings_error_no_email),
            SnackbarType.ERROR
        )
    }
}