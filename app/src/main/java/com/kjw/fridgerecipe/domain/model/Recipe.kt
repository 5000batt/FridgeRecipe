package com.kjw.fridgerecipe.domain.model

enum class LevelType(val label: String) {
    BEGINNER("초급"),
    INTERMEDIATE("중급"),
    ADVANCED("고급"),
    ETC("기타");

    companion object {
        fun fromString(label: String?): LevelType {
            return entries.find { it.label == label } ?: ETC
        }
    }
}

data class RecipeIngredient(
    val name: String,
    val quantity: String
)

data class RecipeStep(
    val number: Int,
    val description: String
)

data class Recipe(
    val id: Long? = null,
    val title: String,
    val servings: String,
    val time: String,
    val level: LevelType,
    val ingredients: List<RecipeIngredient>,
    val steps: List<RecipeStep>,
    // 검색 필터
    val ingredientsQuery: String? = null,
    val timeFilter: String? = null,
    val levelFilter: LevelType? = null,
    val categoryFilter: String? = null,
    val utensilFilter: String? = null,
    val useOnlySelected: Boolean = false
)
