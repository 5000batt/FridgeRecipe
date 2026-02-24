package com.kjw.fridgerecipe.domain.model

/**
 * 앱의 테마 설정을 나타내는 Enum.
 */
enum class ThemeMode(
    val value: Int,
) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2),
    ;

    companion object {
        fun fromValue(value: Int?): ThemeMode = entries.find { it.value == value } ?: SYSTEM
    }
}
