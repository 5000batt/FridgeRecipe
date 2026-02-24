package com.kjw.fridgerecipe.domain.repository

import com.kjw.fridgerecipe.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    // 앱 테마 설정
    val themeMode: Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)

    // 알림 여부 설정
    val isNotificationEnabled: Flow<Boolean>

    suspend fun setNotificationEnabled(enabled: Boolean)

    // 제외 재료 설정
    val excludedIngredients: Flow<Set<String>>

    suspend fun setExcludedIngredients(ingredients: Set<String>)

    // 재료 확인 건너뛰기 설정
    val isIngredientCheckSkip: Flow<Boolean>

    suspend fun setIngredientCheckSkip(isSkip: Boolean)

    // 최초 실행 여부 설정
    val isFirstLaunch: Flow<Boolean>

    suspend fun setFirstLaunchComplete()
}
