package com.kjw.fridgerecipe.domain.util

enum class DataError {
    SAVE_FAILED,
    UPDATE_FAILED,
    DELETE_FAILED,
    UNKNOWN,

    // 재료 관련 에러
    EMPTY_NAME,
    INVALID_AMOUNT,

    // 레시피 관련 에러
    RECIPE_ALREADY_EXISTS,
    RECIPE_NOT_FOUND,

    // AI(Gemini) 관련 에러
    NETWORK_ERROR,
    QUOTA_EXCEEDED,
    SERVER_ERROR,
    API_KEY_ERROR,
    PARSING_ERROR,
    RESPONSE_BLOCKED,
}
