package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.model.ThemeMode
import com.kjw.fridgerecipe.domain.usecase.DeleteAllIngredientsUseCase
import com.kjw.fridgerecipe.domain.usecase.DeleteAllRecipesUseCase
import com.kjw.fridgerecipe.domain.usecase.ObserveExcludedIngredientsUseCase
import com.kjw.fridgerecipe.domain.usecase.ObserveNotificationEnabledUseCase
import com.kjw.fridgerecipe.domain.usecase.ObserveThemeModeUseCase
import com.kjw.fridgerecipe.domain.usecase.SetExcludedIngredientsUseCase
import com.kjw.fridgerecipe.domain.usecase.SetNotificationEnabledUseCase
import com.kjw.fridgerecipe.domain.usecase.SetThemeModeUseCase
import com.kjw.fridgerecipe.presentation.ui.model.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val deleteAllRecipesUseCase: DeleteAllRecipesUseCase,
        private val deleteAllIngredientsUseCase: DeleteAllIngredientsUseCase,
        private val observeThemeModeUseCase: ObserveThemeModeUseCase,
        private val setThemeModeUseCase: SetThemeModeUseCase,
        private val observeNotificationEnabledUseCase: ObserveNotificationEnabledUseCase,
        private val setNotificationEnabledUseCase: SetNotificationEnabledUseCase,
        private val observeExcludedIngredientsUseCase: ObserveExcludedIngredientsUseCase,
        private val setExcludedIngredientsUseCase: SetExcludedIngredientsUseCase,
    ) : ViewModel() {
        private val internalState = MutableStateFlow(SettingsUiState())
        private val _isLoading = MutableStateFlow(true)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        val uiState: StateFlow<SettingsUiState> =
            combine(
                internalState,
                observeThemeModeUseCase(),
                observeNotificationEnabledUseCase(),
                observeExcludedIngredientsUseCase(),
            ) { state, currentThemeMode, isNotiEnabled, excludedSet ->

                val isDarkMode =
                    when (currentThemeMode) {
                        ThemeMode.LIGHT -> false
                        ThemeMode.DARK -> true
                        ThemeMode.SYSTEM -> null
                    }

                state.copy(
                    isDarkMode = isDarkMode,
                    isNotificationEnabled = isNotiEnabled,
                    excludedIngredients = excludedSet.toList().sorted(),
                )
            }.onEach {
                _isLoading.value = false
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SettingsUiState(),
            )

        fun syncNotificationState(isSystemPermissionGranted: Boolean) {
            viewModelScope.launch {
                val currentEnabled = observeNotificationEnabledUseCase().first()

                if (!isSystemPermissionGranted && currentEnabled) {
                    setNotificationEnabledUseCase(false)
                }
            }
        }

        fun toggleNotification(isEnabled: Boolean) {
            viewModelScope.launch {
                setNotificationEnabledUseCase(isEnabled)
            }
        }

        fun showPermissionDialog() {
            internalState.update { it.copy(showPermissionDialog = true) }
        }

        fun dismissPermissionDialog() {
            internalState.update { it.copy(showPermissionDialog = false) }
        }

        fun setTheme(isDark: Boolean?) {
            viewModelScope.launch {
                val mode =
                    when (isDark) {
                        false -> ThemeMode.LIGHT
                        true -> ThemeMode.DARK
                        null -> ThemeMode.SYSTEM
                    }
                setThemeModeUseCase(mode)
            }
        }

        fun onNewExcludedIngredientChanged(text: String) {
            internalState.update { it.copy(newExcludedIngredient = text) }
        }

        fun addExcludedIngredient() {
            val newItem = internalState.value.newExcludedIngredient.trim()
            val currentList = uiState.value.excludedIngredients

            if (newItem.isNotBlank() && newItem !in currentList) {
                viewModelScope.launch {
                    val newSet = currentList.toSet() + newItem
                    setExcludedIngredientsUseCase(newSet)
                    internalState.update { it.copy(newExcludedIngredient = "") }
                }
            }
        }

        fun removeExcludedIngredient(item: String) {
            viewModelScope.launch {
                val currentList = uiState.value.excludedIngredients
                val newSet = currentList.toSet() - item
                setExcludedIngredientsUseCase(newSet)
            }
        }

        fun showResetIngredientsDialog() {
            internalState.update { it.copy(showResetIngredientsDialog = true) }
        }

        fun dismissResetIngredientsDialog() {
            internalState.update { it.copy(showResetIngredientsDialog = false) }
        }

        fun resetIngredients() {
            viewModelScope.launch {
                deleteAllIngredientsUseCase()
                internalState.update { it.copy(showResetIngredientsDialog = false) }
            }
        }

        fun showResetRecipesDialog() {
            internalState.update { it.copy(showResetRecipesDialog = true) }
        }

        fun dismissResetRecipesDialog() {
            internalState.update { it.copy(showResetRecipesDialog = false) }
        }

        fun resetRecipes() {
            viewModelScope.launch {
                deleteAllRecipesUseCase()
                internalState.update { it.copy(showResetRecipesDialog = false) }
            }
        }
    }
