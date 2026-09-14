package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFirstLaunchUseCase
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) {
        operator fun invoke(): Flow<Boolean> = settingsRepository.isFirstLaunch
    }
