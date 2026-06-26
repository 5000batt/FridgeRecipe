package com.kjw.fridgerecipe.data.repository

import com.kjw.fridgerecipe.domain.util.DataError
import com.kjw.fridgerecipe.domain.util.DataResult

internal suspend inline fun <T> safeDbCall(
    error: DataError = DataError.UNKNOWN,
    crossinline block: suspend () -> T,
): DataResult<T> =
    try {
        DataResult.Success(block())
    } catch (e: Exception) {
        DataResult.Error(error = error, cause = e)
    }
