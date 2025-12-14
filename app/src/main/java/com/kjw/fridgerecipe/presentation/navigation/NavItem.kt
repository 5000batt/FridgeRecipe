package com.kjw.fridgerecipe.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed interface Destination {
    val route: String
    val title: String
    val isRoot: Boolean
}

enum class MainTab(
    override val route: String,
    override val title: String,
    val icon: ImageVector,
    val label: String
) : Destination {
    HOME("home", "Fridge Recipe", Icons.Default.Home, "홈"),
    INGREDIENTS("ingredients", "전체 재료 목록", Icons.Default.Kitchen, "재료"),
    RECIPES("recipes", "레시피", Icons.Default.RestaurantMenu, "레시피"),
    SETTINGS("settings", "환경설정", Icons.Default.Settings, "환경설정");

    override val isRoot: Boolean = true

    companion object {
        fun getByRoute(route: String?): MainTab? = entries.find { it.route == route }
    }
}

sealed class DetailDestination(
    override val route: String,
    override val title: String
) : Destination {
    override val isRoot: Boolean = false

    data object IngredientEdit : DetailDestination(
        route = "ingredient_edit?ingredientId={ingredientId}",
        title = "재료 상세"
    ) {
        const val ARG_ID = "ingredientId"
        const val DEFAULT_ID = -1L

        val arguments: List<NamedNavArgument> = listOf(
            navArgument(ARG_ID) { type = NavType.LongType; defaultValue = DEFAULT_ID}
        )

        fun createRoute(ingredientId: Long = DEFAULT_ID) = "ingredient_edit?ingredientId=$ingredientId"
        fun getTitle(isNew: Boolean) = if (isNew) "새 재료 추가" else "재료 수정"
    }

    data object RecipeDetail : DetailDestination(
        route = "recipe_detail/{recipeId}",
        title = "레시피 상세"
    ) {
        const val ARG_ID = "recipeId"

        val arguments: List<NamedNavArgument> = listOf(
            navArgument(ARG_ID) { type = NavType.LongType }
        )

        fun createRoute(recipeId: Long) = "recipe_detail/$recipeId"
    }

    data object RecipeEdit : DetailDestination(
        route = "recipe_edit?recipeId={recipeId}",
        title = "레시피 작성"
    ) {
        const val ARG_ID = "recipeId"
        const val DEFAULT_ID = -1L

        val arguments: List<NamedNavArgument> = listOf(
            navArgument(ARG_ID) { type = NavType.LongType }
        )

        fun createRoute(recipeId: Long = DEFAULT_ID) = "recipe_edit?recipeId=$recipeId"
    }

    companion object {
        fun getByRoute(route: String?): DetailDestination? {
            if(route == null) return null
            return when {
                route.startsWith("ingredient_edit") -> IngredientEdit
                route.startsWith("recipe_detail") -> RecipeDetail
                route.startsWith("recipe_edit") -> RecipeEdit
                else -> null
            }
        }
    }
}
