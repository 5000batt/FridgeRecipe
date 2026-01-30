package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.repository.ImageRepository
import com.kjw.fridgerecipe.domain.util.DataResult
import javax.inject.Inject

class SaveRecipeImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(uriString: String): DataResult<String> {
        return imageRepository.saveImageToInternalStorage(uriString)
    }
}
