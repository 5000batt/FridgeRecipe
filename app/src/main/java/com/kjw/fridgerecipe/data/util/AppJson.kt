package com.kjw.fridgerecipe.data.util

import kotlinx.serialization.json.Json

/**
 * 앱 전체에서 공통으로 사용할 Json 인스턴스 설정.
 * 싱글톤으로 관리하여 메모리 오버헤드를 줄이고 일관된 직렬화 정책을 유지합니다.
 */
object AppJson {
    val default = Json {
        ignoreUnknownKeys = true    // 정의되지 않은 키는 무시
        encodeDefaults = true       // 기본값 포함 직렬화
        isLenient = true            // 관대한 파싱 (따옴표 누락 등 허용)
        coerceInputValues = true    // Null 등이 올 경우 기본값으로 강제 변환
    }
}
