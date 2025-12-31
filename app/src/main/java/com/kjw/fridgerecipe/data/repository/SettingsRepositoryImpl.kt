package com.kjw.fridgerecipe.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kjw.fridgerecipe.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val THEME_MODE = intPreferencesKey("theme_mode") // 0: 시스템, 1: 라이트, 2: 다크
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val EXCLUDED_INGREDIENTS = stringSetPreferencesKey("excluded_ingredients")
        val INGREDIENT_CHECK_SKIP = booleanPreferencesKey("ingredient_check_skip")
    }

    override val themeMode: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.THEME_MODE] ?: 0
        }

    override suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    override val isNotificationEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_ENABLED] ?: true
        }

    override suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_ENABLED] = enabled
        }
    }

    override val excludedIngredients: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.EXCLUDED_INGREDIENTS] ?: emptySet()
        }

    override suspend fun setExcludedIngredients(ingredients: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EXCLUDED_INGREDIENTS] = ingredients
        }
    }

    override val isIngredientCheckSkip: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.INGREDIENT_CHECK_SKIP] ?: false
        }

    override suspend fun setIngredientCheckSkip(isSkip: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INGREDIENT_CHECK_SKIP] = isSkip
        }
    }

}