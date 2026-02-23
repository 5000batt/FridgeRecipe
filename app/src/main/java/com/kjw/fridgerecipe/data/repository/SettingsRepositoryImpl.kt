package com.kjw.fridgerecipe.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.kjw.fridgerecipe.di.SettingsDataStore
import com.kjw.fridgerecipe.domain.model.ThemeMode
import com.kjw.fridgerecipe.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    @SettingsDataStore private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object PreferencesKeys {
        val THEME_MODE = intPreferencesKey("theme_mode")
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val EXCLUDED_INGREDIENTS = stringSetPreferencesKey("excluded_ingredients")
        val INGREDIENT_CHECK_SKIP = booleanPreferencesKey("ingredient_check_skip")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    }

    override val themeMode: Flow<ThemeMode> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e("SettingsRepo", "Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val modeValue = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.value
            ThemeMode.fromValue(modeValue)
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.value
        }
    }

    override val isNotificationEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_ENABLED] ?: true
        }

    override suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_ENABLED] = enabled
        }
    }

    override val excludedIngredients: Flow<Set<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.EXCLUDED_INGREDIENTS] ?: emptySet()
        }

    override suspend fun setExcludedIngredients(ingredients: Set<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.EXCLUDED_INGREDIENTS] = ingredients
        }
    }

    override val isIngredientCheckSkip: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.INGREDIENT_CHECK_SKIP] ?: false
        }

    override suspend fun setIngredientCheckSkip(isSkip: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.INGREDIENT_CHECK_SKIP] = isSkip
        }
    }

    override val isFirstLaunch: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true
        }

    override suspend fun setFirstLaunchComplete() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = false
        }
    }
}
