package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.model.ThemeMode
import com.kjw.fridgerecipe.domain.repository.SettingsRepository
import javax.inject.Inject

class SetThemeModeUseCase
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) {
        suspend operator fun invoke(mode: ThemeMode) = settingsRepository.setThemeMode(mode)
    }
