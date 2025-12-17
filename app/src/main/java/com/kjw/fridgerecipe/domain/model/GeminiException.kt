package com.kjw.fridgerecipe.domain.model

sealed class GeminiException(message: String) : Exception(message) {
    class QuotaExceeded : GeminiException("일일 무료 사용량이 초과되었습니다.") // 429
    class ServerOverloaded : GeminiException("AI 서버가 혼잡하여 응답할 수 없습니다.") // 503
    class InvalidRequest : GeminiException("요청 형식이 올바르지 않습니다.") // 400
    class ApiKeyError : GeminiException("API 인증에 실패했습니다.") // 403
    class Unknown(val code: Int) : GeminiException("알 수 없는 오류가 발생했습니다. (코드: $code)")
    class ParsingError : GeminiException("AI 응답을 분석하는 중 오류가 발생했습니다.") // JSON 파싱 실패
}