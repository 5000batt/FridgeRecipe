package com.kjw.fridgerecipe.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<Int>
    suspend fun setThemeMode(mode: Int)

    val isNotificationEnabled: Flow<Boolean>
    suspend fun setNotificationEnabled(enabled: Boolean)

    val excludedIngredients: Flow<Set<String>>
    suspend fun setExcludedIngredients(ingredients: Set<String>)
}