package com.kjw.fridgerecipe.domain.model

sealed class GeminiException(
    message: String,
) : Exception(message) {
    class QuotaExceeded : GeminiException("사용량 초과 (429)")

    class ServerOverloaded : GeminiException("서버 혼잡 (503)")

    class ApiKeyError : GeminiException("API 키 오류 (403)")

    class ParsingError : GeminiException("응답 파싱 실패")

    class NetworkError : GeminiException("네트워크 연결 실패")

    class ResponseBlocked : GeminiException("안전 정책 차단")

    class Unknown(
        val code: Int,
    ) : GeminiException("알 수 없는 오류 ($code)")
}
