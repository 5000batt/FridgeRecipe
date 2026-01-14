package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.repository.SettingsRepository
import com.kjw.fridgerecipe.domain.usecase.DeleteAllIngredientsUseCase
import com.kjw.fridgerecipe.domain.usecase.DeleteAllRecipesUseCase
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
class SettingsViewModel @Inject constructor(
    private val deleteAllRecipesUseCase: DeleteAllRecipesUseCase,
    private val deleteAllIngredientsUseCase: DeleteAllIngredientsUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _internalState = MutableStateFlow(SettingsUiState())
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        _internalState,
        settingsRepository.themeMode,
        settingsRepository.isNotificationEnabled,
        settingsRepository.excludedIngredients
    ) { state, themeModeInt, isNotiEnabled, excludedSet ->

        val isDarkMode = when (themeModeInt) {
            1 -> false
            2 -> true
            else -> null
        }

        state.copy(
            isDarkMode = isDarkMode,
            isNotificationEnabled = isNotiEnabled,
            excludedIngredients = excludedSet.toList().sorted()
        )
    }
    .onEach {
        _isLoading.value = false
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun syncNotificationState(isSystemPermissionGranted: Boolean) {
        viewModelScope.launch {
            val currentEnabled = settingsRepository.isNotificationEnabled.first()

            if (!isSystemPermissionGranted && currentEnabled) {
                settingsRepository.setNotificationEnabled(false)
            }
        }
    }

    fun toggleNotification(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationEnabled(isEnabled)
        }
    }

    fun showPermissionDialog() {
        _internalState.update { it.copy(showPermissionDialog = true) }
    }

    fun dismissPermissionDialog() {
        _internalState.update { it.copy(showPermissionDialog = false) }
    }

    fun setTheme(isDark: Boolean?) {
        viewModelScope.launch {
            val mode = when (isDark) {
                false -> 1
                true -> 2
                null -> 0
            }
            settingsRepository.setThemeMode(mode)
        }
    }

    fun onNewExcludedIngredientChanged(text: String) {
        _internalState.update { it.copy(newExcludedIngredient = text) }
    }

    fun addExcludedIngredient() {
        val newItem = _internalState.value.newExcludedIngredient.trim()
        val currentList = uiState.value.excludedIngredients

        if (newItem.isNotBlank() && newItem !in currentList) {
            viewModelScope.launch {
                val newSet = currentList.toSet() + newItem
                settingsRepository.setExcludedIngredients(newSet)
                _internalState.update { it.copy(newExcludedIngredient = "") }
            }
        }
    }

    fun removeExcludedIngredient(item: String) {
        viewModelScope.launch {
            val currentList = uiState.value.excludedIngredients
            val newSet = currentList.toSet() - item
            settingsRepository.setExcludedIngredients(newSet)
        }
    }

    fun showResetIngredientsDialog() {
        _internalState.update { it.copy(showResetIngredientsDialog = true) }
    }

    fun dismissResetIngredientsDialog() {
        _internalState.update { it.copy(showResetIngredientsDialog = false) }
    }

    fun resetIngredients() {
        viewModelScope.launch {
            deleteAllIngredientsUseCase()
            _internalState.update { it.copy(showResetIngredientsDialog = false) }
        }
    }

    fun showResetRecipesDialog() {
        _internalState.update { it.copy(showResetRecipesDialog = true) }
    }

    fun dismissResetRecipesDialog() {
        _internalState.update { it.copy(showResetRecipesDialog = false) }
    }

    fun resetRecipes() {
        viewModelScope.launch {
            deleteAllRecipesUseCase()
            _internalState.update { it.copy(showResetRecipesDialog = false) }
        }
    }
}