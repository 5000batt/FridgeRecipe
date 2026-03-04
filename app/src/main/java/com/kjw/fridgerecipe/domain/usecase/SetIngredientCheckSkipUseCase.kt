package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.repository.SettingsRepository
import javax.inject.Inject

class SetIngredientCheckSkipUseCase
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) {
        suspend operator fun invoke(isSkip: Boolean) = settingsRepository.setIngredientCheckSkip(isSkip)
    }
