package com.kjw.fridgerecipe.presentation.navigation

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.kjw.fridgerecipe.R
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

// 메인 탭
@Serializable
data object HomeRoute

@Serializable
data object IngredientListRoute

@Serializable
data object RecipeListRoute

// 상세 화면
@Serializable
data class IngredientEditRoute(
    val ingredientId: Long = -1L,
    val categoryId: String? = null,
) {
    companion object {
        const val DEFAULT_ID = -1L
    }
}

@Serializable
data class RecipeDetailRoute(
    val recipeId: Long,
)

@Serializable
data class RecipeEditRoute(
    val recipeId: Long = -1L,
) {
    companion object {
        const val DEFAULT_ID = -1L
    }
}

@Serializable
data object SettingsRoute

enum class MainTab(
    val route: Any,
    val routeClass: KClass<*>,
    @StringRes val titleResId: Int,
    val icon: ImageVector,
    @StringRes val labelResId: Int,
) {
    HOME(
        route = HomeRoute,
        routeClass = HomeRoute::class,
        titleResId = R.string.title_home,
        icon = Icons.Default.Home,
        labelResId = R.string.label_tab_home,
    ),
    INGREDIENTS(
        route = IngredientListRoute,
        routeClass = IngredientListRoute::class,
        titleResId = R.string.title_ingredient_list,
        icon = Icons.Default.Kitchen,
        labelResId = R.string.label_tab_ingredients,
    ),
    RECIPES(
        route = RecipeListRoute,
        routeClass = RecipeListRoute::class,
        titleResId = R.string.title_recipe_list,
        icon = Icons.Default.RestaurantMenu,
        labelResId = R.string.label_tab_recipes,
    ),
    ;

    @SuppressLint("RestrictedApi")
    fun isSelected(currentDestination: NavDestination?): Boolean = currentDestination?.hasRoute(this.routeClass) == true
}
