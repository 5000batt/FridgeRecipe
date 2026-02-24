package com.kjw.fridgerecipe.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 재료의 소비기한 상태를 나타내는 Enum.
 */
enum class ExpirationStatus {
    EXPIRED, // 기한 지남
    URGENT, // 기한 임박 (보통 3일 이내)
    SAFE, // 기한 여유
    ;

    companion object {
        fun from(expirationDate: LocalDate): ExpirationStatus {
            val today = LocalDate.now()
            val daysLeft = ChronoUnit.DAYS.between(today, expirationDate)

            return when {
                daysLeft < 0 -> EXPIRED
                daysLeft <= 3 -> URGENT
                else -> SAFE
            }
        }
    }
}
