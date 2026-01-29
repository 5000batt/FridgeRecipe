package com.kjw.fridgerecipe.data.repository

import android.util.Log
import com.kjw.fridgerecipe.data.datasource.RecipeRemoteDataSource
import com.kjw.fridgerecipe.data.local.dao.RecipeDao
import com.kjw.fridgerecipe.data.repository.mapper.toDomainModel
import com.kjw.fridgerecipe.data.repository.mapper.toEntity
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeSearchMetadata
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
        levelFilter: LevelType?,
        categoryFilter: String?,
        utensilFilter: String?,
        useOnlySelected: Boolean,
        excludedIngredients: List<String>
    ): Recipe? {

        // 필터 정리
        val safeTime = sanitizeFilter(timeFilter)
        val safeCategory = sanitizeFilter(categoryFilter)
        val safeUtensil = sanitizeFilter(utensilFilter)

        // AI 호출 및 응답 확인
        val recipeDto = remoteDataSource.getAiRecipe(
            ingredients = ingredients,
            timeFilter = safeTime,
            levelFilter = levelFilter,
            categoryFilter = safeCategory,
            utensilFilter = safeUtensil,
            useOnlySelected = useOnlySelected,
            excludedIngredients = excludedIngredients
        )

        // 도메인 변환
        val domainRecipe = recipeDto.toDomainModel()

        // 메타데이터 생성 및 저장
        val searchMetadata = RecipeSearchMetadata(
            ingredientsQuery = ingredientsQuery,
            timeFilter = safeTime,
            levelFilter = levelFilter,
            categoryFilter = safeCategory,
            utensilFilter = safeUtensil,
            useOnlySelected = useOnlySelected
        )

        val savedId = saveOrUpdateRecipe(domainRecipe, ingredientsQuery, searchMetadata)

        return domainRecipe.copy(
            id = savedId,
            searchMetadata = searchMetadata
        )
    }

    private suspend fun saveOrUpdateRecipe(
        domainRecipe: Recipe,
        ingredientsQuery: String,
        metadata: RecipeSearchMetadata
    ): Long {
        val existingEntity = recipeDao.findExistingRecipe(
            title = domainRecipe.title,
            ingredientsQuery = ingredientsQuery
        )

        return if (existingEntity != null) {
            Log.d("RecipeRepo", "기존 레시피 발견 (업데이트 안 함): ${domainRecipe.title} (ID: ${existingEntity.id})")
            existingEntity.id ?: throw IllegalStateException("기존 레시피의 ID가 누락되었습니다.")
        } else {
            Log.d("RecipeRepo", "새 레시피 삽입: ${domainRecipe.title}")
            val newRecipe = domainRecipe.copy(searchMetadata = metadata)
            recipeDao.insertRecipe(newRecipe.toEntity())
        }
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
        timeFilter: String?,
        levelFilter: LevelType?,
        categoryFilter: String?,
        utensilFilter: String?,
        useOnlySelected: Boolean
    ): List<Recipe> {
        val entities = recipeDao.findRecipesByFilters(
            ingredientsQuery =  ingredientsQuery,
            timeFilter = sanitizeFilter(timeFilter),
            levelFilter = levelFilter?.label,
            categoryFilter = sanitizeFilter(categoryFilter),
            utensilFilter = sanitizeFilter(utensilFilter),
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