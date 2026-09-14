package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.repository.SettingsRepository
import javax.inject.Inject

class SetNotificationEnabledUseCase
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) {
        suspend operator fun invoke(enabled: Boolean) = settingsRepository.setNotificationEnabled(enabled)
    }
