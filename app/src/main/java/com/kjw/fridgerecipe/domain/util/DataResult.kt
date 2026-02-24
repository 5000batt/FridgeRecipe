package com.kjw.fridgerecipe.domain.util

import com.kjw.fridgerecipe.presentation.util.UiText

/**
 * 데이터 요청 결과를 캡슐화하는 sealed class.
 */
sealed class DataResult<out T> {
    data class Success<out T>(
        val data: T,
    ) : DataResult<T>()

    data class Error(
        val message: UiText,
        val title: UiText? = null,
        val cause: Throwable? = null,
    ) : DataResult<Nothing>()

    data object Loading : DataResult<Nothing>()

    /**
     * 성공 시 데이터를 반환하고, 그 외의 경우 null을 반환합니다.
     */
    fun getOrNull(): T? = if (this is Success) data else null
}
