package com.kjw.fridgerecipe.presentation.ui.components.recipe.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.presentation.ui.components.common.CommonDropdown
import com.kjw.fridgerecipe.presentation.util.RecipeConstants

@Composable
fun RecipeMetadataForm(
    selectedLevel: LevelType,
    onLevelChange: (LevelType) -> Unit,
    categoryState: String,
    onCategoryChange: (String) -> Unit,
    utensilState: String,
    onUtensilChange: (String) -> Unit
) {
    val context = LocalContext.current

    fun getLevelLabel(level: LevelType): String {
        return RecipeConstants.LEVEL_FILTER_OPTIONS
            .find { it.value == level }?.label?.asString(context)
            ?: level.label
    }

    fun getCategoryLabel(value: String): String {
        return RecipeConstants.CATEGORY_FILTER_OPTIONS
            .find { it.value == value }?.label?.asString(context)
            ?: value
    }

    fun getUtensilLabel(value: String): String {
        return RecipeConstants.UTENSIL_FILTER_OPTIONS
            .find { it.value == value }?.label?.asString(context)
            ?: value
    }

    Column {
        // 난이도
        CommonDropdown(
            value = getLevelLabel(selectedLevel),
            label = stringResource(R.string.recipe_edit_label_level),
            options = RecipeConstants.LEVEL_FILTER_OPTIONS.filter { it.value != null },
            onOptionSelected = { option -> option.value?.let { onLevelChange(it) } },
            itemLabel = { it.label.asString() }
        )
        Spacer(modifier = Modifier.Companion.height(12.dp))

        // 음식 종류
        CommonDropdown(
            value = getCategoryLabel(categoryState),
            label = stringResource(R.string.recipe_edit_label_category),
            options = RecipeConstants.CATEGORY_FILTER_OPTIONS,
            onOptionSelected = { onCategoryChange(it.value) },
            itemLabel = { it.label.asString() }
        )
        Spacer(modifier = Modifier.Companion.height(12.dp))

        // 조리 도구
        CommonDropdown(
            value = getUtensilLabel(utensilState),
            label = stringResource(R.string.recipe_edit_label_utensil),
            options = RecipeConstants.UTENSIL_FILTER_OPTIONS,
            onOptionSelected = { onUtensilChange(it.value) },
            itemLabel = { it.label.asString() }
        )
    }
}