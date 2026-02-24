package com.kjw.fridgerecipe.domain.usecase

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CheckIngredientConflictsUseCase
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) {
        suspend operator fun invoke(selectedIngredients: List<Ingredient>): List<String> {
            val excludedList = settingsRepository.excludedIngredients.first().toSet()
            return selectedIngredients
                .map { it.name }
                .filter { it in excludedList }
        }
    }
