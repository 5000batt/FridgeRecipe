package com.kjw.fridgerecipe.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    // 앱 테마 설정
    val themeMode: Flow<Int>
    suspend fun setThemeMode(mode: Int)

    // 알림 여부 설정
    val isNotificationEnabled: Flow<Boolean>
    suspend fun setNotificationEnabled(enabled: Boolean)

    // 제외 재료 설정
    val excludedIngredients: Flow<Set<String>>
    suspend fun setExcludedIngredients(ingredients: Set<String>)

    // 재료 확인 건너뛰기 설정
    val isIngredientCheckSkip: Flow<Boolean>
    suspend fun setIngredientCheckSkip(isSkip: Boolean)
}