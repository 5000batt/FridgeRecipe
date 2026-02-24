package com.kjw.fridgerecipe.presentation.ui.components.recipe.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.CookingToolType
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonDropdown
import com.kjw.fridgerecipe.presentation.util.RecipeConstants

@Composable
fun RecipeMetadataForm(
    selectedLevel: LevelType,
    onLevelChange: (LevelType) -> Unit,
    categoryState: RecipeCategoryType?,
    onCategoryChange: (RecipeCategoryType?) -> Unit,
    cookingToolState: CookingToolType?,
    onCookingToolChange: (CookingToolType?) -> Unit,
) {
    Column {
        // 난이도
        CommonDropdown(
            value = stringResource(selectedLevel.labelResId),
            label = stringResource(R.string.recipe_edit_label_level),
            options = RecipeConstants.LEVEL_FILTER_OPTIONS.filter { it.value != null },
            onOptionSelected = { option -> option.value?.let { onLevelChange(it) } },
            itemLabel = { it.label.asString() },
        )
        Spacer(modifier = Modifier.Companion.height(12.dp))

        // 음식 종류
        CommonDropdown(
            value = categoryState?.let { stringResource(it.labelResId) } ?: stringResource(R.string.filter_any),
            label = stringResource(R.string.recipe_edit_label_category),
            options = RecipeConstants.CATEGORY_FILTER_OPTIONS,
            onOptionSelected = { option ->
                onCategoryChange(option.value)
            },
            itemLabel = { it.label.asString() },
        )
        Spacer(modifier = Modifier.Companion.height(12.dp))

        // 조리 도구
        CommonDropdown(
            value = cookingToolState?.let { stringResource(it.labelResId) } ?: stringResource(R.string.filter_any),
            label = stringResource(R.string.recipe_edit_label_cooking_tool),
            options = RecipeConstants.COOKING_TOOL_FILTER_OPTIONS,
            onOptionSelected = { option ->
                onCookingToolChange(option.value)
            },
            itemLabel = { it.label.asString() },
        )
    }
}
