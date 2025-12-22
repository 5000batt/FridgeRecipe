package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.repository.ImageRepository
import javax.inject.Inject

class SaveRecipeImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend operator fun  invoke(uriString: String): String? {
        return imageRepository.saveImageToInternalStorage(uriString)
    }
}