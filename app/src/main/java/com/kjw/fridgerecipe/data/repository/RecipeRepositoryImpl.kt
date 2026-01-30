package com.kjw.fridgerecipe.data.repository

import android.util.Log
import com.kjw.fridgerecipe.data.datasource.RecipeRemoteDataSource
import com.kjw.fridgerecipe.data.local.dao.RecipeDao
import com.kjw.fridgerecipe.data.repository.mapper.toDomainModel
import com.kjw.fridgerecipe.data.repository.mapper.toEntity
import com.kjw.fridgerecipe.domain.model.CookingToolType
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import com.kjw.fridgerecipe.presentation.util.RecipeConstants.FILTER_ANY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val recipeDao: RecipeDao,
    private val remoteDataSource: RecipeRemoteDataSource
) : RecipeRepository {

    private fun sanitizeFilter(value: String?): String? {
        return if (value == FILTER_ANY || value.isNullOrBlank()) null else value
    }

    override suspend fun getAiRecipes(
        ingredients: List<Ingredient>,
        ingredientsQuery: String,
        timeFilter: String?,
        level: LevelType?,
        categoryFilter: RecipeCategoryType?,
        cookingToolFilter: CookingToolType?,
        useOnlySelected: Boolean,
        excludedIngredients: List<String>
    ): Recipe? {

        val safeTime = sanitizeFilter(timeFilter)

        // AI 호출 및 응답 확인
        val recipeDto = remoteDataSource.getAiRecipe(
            ingredients = ingredients,
            timeFilter = safeTime,
            level = level,
            categoryFilter = categoryFilter,
            cookingToolFilter = cookingToolFilter,
            useOnlySelected = useOnlySelected,
            excludedIngredients = excludedIngredients
        )

        // 도메인 변환 및 검색 조건 설정 (캐싱 키)
        val domainRecipe = recipeDto.toDomainModel().copy(
            category = categoryFilter,
            cookingTool = cookingToolFilter,
            timeFilter = safeTime,
            level = level ?: recipeDto.toDomainModel().level,
            ingredientsQuery = ingredientsQuery,
            useOnlySelected = useOnlySelected
        )

        // 중복 체크 및 저장 로직
        val existingEntity = recipeDao.findExistingRecipeByTitle(domainRecipe.title, ingredientsQuery)

        val savedId = if (existingEntity != null) {
            Log.d("RecipeRepo", "기존 레시피 발견 (ID 유지): ${domainRecipe.title}")
            existingEntity.id ?: throw IllegalStateException("기존 레시피의 ID가 누락되었습니다.")
        } else {
            Log.d("RecipeRepo", "새 레시피 삽입: ${domainRecipe.title}")
            recipeDao.insertRecipe(domainRecipe.toEntity())
        }

        return domainRecipe.copy(id = savedId)
    }

    override fun getAllSavedRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes()
            .map { entityList ->
                entityList.map { it.toDomainModel() }
            }
    }

    override suspend fun getSavedRecipeById(id: Long): Recipe? {
        return recipeDao.getRecipeById(id)?.toDomainModel()
    }

    override suspend fun findRecipesByFilters(
        ingredientsQuery: String,
        categoryFilter: RecipeCategoryType?,
        cookingToolFilter: CookingToolType?,
        timeFilter: String?,
        level: LevelType?,
        useOnlySelected: Boolean
    ): List<Recipe> {
        val entities = recipeDao.findRecipesByFilters(
            ingredientsQuery = ingredientsQuery,
            category = categoryFilter,
            cookingTool = cookingToolFilter,
            timeFilter = sanitizeFilter(timeFilter),
            level = level,
            useOnlySelected = useOnlySelected
        )
        return entities.map { it.toDomainModel() }
    }

    override suspend fun insertRecipe(recipe: Recipe) {
        recipeDao.insertRecipe(recipe.toEntity())
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        recipeDao.updateRecipe(recipe.toEntity())
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.deleteRecipe(recipe.toEntity())
    }

    override suspend fun deleteAllRecipes() {
        recipeDao.deleteAllRecipes()
    }
}
