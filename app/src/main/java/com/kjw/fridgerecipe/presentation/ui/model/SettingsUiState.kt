package com.kjw.fridgerecipe.presentation.ui.model

data class SettingsUiState(
    // 알림 설정
    val isNotificationEnabled: Boolean? = null,
    val expiryAlertDays: Int = 3,

    // 일반 설정
    val isDarkMode: Boolean? = null,
    val excludedIngredients: List<String> = emptyList(),

    // 앱 정보
    val appVersion: String = "1.0.0",

    // UI 제어
    val showResetIngredientsDialog: Boolean = false,
    val showResetRecipesDialog: Boolean = false,
    val showPermissionDialog: Boolean = false,

    // 입력 필드
    val newExcludedIngredient: String = ""
)
