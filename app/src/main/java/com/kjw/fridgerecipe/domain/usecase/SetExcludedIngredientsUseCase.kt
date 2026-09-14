package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.repository.SettingsRepository
import javax.inject.Inject

class SetExcludedIngredientsUseCase
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) {
        suspend operator fun invoke(ingredients: Set<String>) = settingsRepository.setExcludedIngredients(ingredients)
    }
