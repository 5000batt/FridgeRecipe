package com.kjw.fridgerecipe.domain.repository

import com.kjw.fridgerecipe.domain.util.DataResult

interface ImageRepository {
    /**
     * 외부 Uri의 이미지를 내부 저장소로 복사 및 압축하여 저장합니다.
     * 성공 시 저장된 파일의 절대 경로를 반환합니다.
     */
    suspend fun saveImageToInternalStorage(uriString: String): DataResult<String>
}
