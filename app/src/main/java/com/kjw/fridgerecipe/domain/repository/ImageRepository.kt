package com.kjw.fridgerecipe.domain.repository

interface ImageRepository {
    suspend fun saveImageToInternalStorage(uriString: String): String?
}