package com.kjw.fridgerecipe.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 재료 도메인 모델.
 * 단순한 데이터 구조를 넘어 소비기한 계산 등의 비즈니스 로직을 포함합니다.
 */
data class Ingredient(
    val id: Long? = null,
    val name: String,
    val amount: Double,
    val unit: UnitType,
    val expirationDate: LocalDate,
    val storageLocation: StorageType,
    val emoticon: IngredientIcon,
    val category: IngredientCategoryType,
) {
    /**
     * 오늘을 기준으로 남은 소비기한 일수. (지났을 경우 음수)
     */
    val daysLeft: Long
        get() = ChronoUnit.DAYS.between(LocalDate.now(), expirationDate)

    /**
     * 현재 재료의 소비기한 상태 (지남, 임박, 여유).
     */
    val expirationStatus: ExpirationStatus
        get() = ExpirationStatus.from(expirationDate)

    /**
     * 사용자가 보기 쉬운 남은 기간 텍스트 반환.
     */
    val remainingDaysText: String
        get() =
            when {
                daysLeft < 0 -> "기한 지남"
                daysLeft == 0L -> "오늘까지"
                else -> "D-$daysLeft"
            }
}
