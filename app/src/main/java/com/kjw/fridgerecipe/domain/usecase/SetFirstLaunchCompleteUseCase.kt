package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.repository.SettingsRepository
import javax.inject.Inject

class SetFirstLaunchCompleteUseCase
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) {
        suspend operator fun invoke() = settingsRepository.setFirstLaunchComplete()
    }
