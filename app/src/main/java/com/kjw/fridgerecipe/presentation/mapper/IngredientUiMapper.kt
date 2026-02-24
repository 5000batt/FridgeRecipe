package com.kjw.fridgerecipe.presentation.mapper

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.presentation.ui.model.IngredientEditUiState
import javax.inject.Inject

/**
 * 재료 UI 관련 데이터 변환을 담당하는 매퍼 클래스
 */
class IngredientUiMapper
    @Inject
    constructor() {
        /**
         * Domain Model(Ingredient)을 UI State(IngredientEditUiState)로 변환합니다.
         * @param ingredient 변환할 도메인 모델
         * @return 변환된 UI 상태 객체
         */
        fun toEditUiState(ingredient: Ingredient): IngredientEditUiState {
            val amountString =
                if (ingredient.amount % 1.0 == 0.0) {
                    ingredient.amount.toInt().toString()
                } else {
                    ingredient.amount.toString()
                }

            return IngredientEditUiState(
                name = ingredient.name,
                amount = amountString,
                selectedUnit = ingredient.unit,
                selectedDate = ingredient.expirationDate,
                selectedStorage = ingredient.storageLocation,
                selectedCategory = ingredient.category,
                selectedIcon = ingredient.emoticon,
                selectedIconCategory = ingredient.emoticon.category,
            )
        }

        /**
         * UI State(IngredientEditUiState)를 Domain Model(Ingredient)로 변환합니다.
         * @param state 현재 UI 상태
         * @param id 수정 모드일 경우 기존 재료의 ID
         * @return 변환된 도메인 모델 객체
         */
        fun toDomain(
            state: IngredientEditUiState,
            id: Long? = null,
        ): Ingredient =
            Ingredient(
                id = id,
                name = state.name,
                amount = state.amount.toDoubleOrNull() ?: 0.0,
                unit = state.selectedUnit,
                expirationDate = state.selectedDate,
                storageLocation = state.selectedStorage,
                category = state.selectedCategory,
                emoticon = state.selectedIcon,
            )
    }
