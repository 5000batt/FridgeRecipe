package com.kjw.fridgerecipe.presentation.ui.model

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.domain.model.IngredientCategoryType

data class HomeUiState(
    // 재료 로딩 상태
    val isIngredientLoading: Boolean = true,

    // 재료 데이터
    val storageIngredients: Map<StorageType, List<Ingredient>> = emptyMap(),
    val allIngredients: List<Ingredient> = emptyList(),

    // 레시피 추천 관련
    val recommendedRecipe: Recipe? = null,
    val isRecipeLoading: Boolean = false,
    val isAdLoaded: Boolean = false, // 광고 로드 상태 추가
    val selectedIngredientIds: Set<Long> = emptySet(),
    val remainingTickets: Int = 3,

    // 필터 및 다이얼로그
    val filterState: RecipeFilterState = RecipeFilterState(),
    val showConflictDialog: Boolean = false,
    val conflictIngredients: List<String> = emptyList(),
    val errorDialogState: ErrorDialogState? = null,
    val showAdDialog: Boolean = false,

    // 현재 선택된 카테고리
    val selectedCategory: IngredientCategoryType? = null,

    // 재료 확인 관련
    val showIngredientCheckDialog: Boolean = false,
    val isIngredientCheckSkip: Boolean = false,

    // 최초 실행 여부
    val isFirstLaunch: Boolean = false
)
