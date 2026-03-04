package com.kjw.fridgerecipe.data.repository

import android.util.Log
import com.kjw.fridgerecipe.data.datasource.RecipeRemoteDataSource
import com.kjw.fridgerecipe.data.local.dao.RecipeDao
import com.kjw.fridgerecipe.data.remote.GeminiException
import com.kjw.fridgerecipe.data.repository.mapper.toDomainModel
import com.kjw.fridgerecipe.data.repository.mapper.toEntity
import com.kjw.fridgerecipe.domain.model.CookingToolType
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import com.kjw.fridgerecipe.domain.util.DataError
import com.kjw.fridgerecipe.domain.util.DataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecipeRepositoryImpl
    @Inject
    constructor(
        private val recipeDao: RecipeDao,
        private val remoteDataSource: RecipeRemoteDataSource,
    ) : RecipeRepository {
        private companion object {
            const val TAG = "RecipeRepo"
        }

        override suspend fun getAiRecipes(
            ingredients: List<Ingredient>,
            ingredientsQuery: String,
            timeFilter: String?,
            level: LevelType?,
            categoryFilter: RecipeCategoryType?,
            cookingToolFilter: CookingToolType?,
            useOnlySelected: Boolean,
            excludedIngredients: List<String>,
        ): DataResult<Recipe> =
            try {
                val recipeDto =
                    remoteDataSource.getAiRecipe(
                        ingredients = ingredients,
                        timeFilter = timeFilter,
                        level = level,
                        categoryFilter = categoryFilter,
                        cookingToolFilter = cookingToolFilter,
                        useOnlySelected = useOnlySelected,
                        excludedIngredients = excludedIngredients,
                    )

                val domainRecipe =
                    recipeDto.toDomainModel().copy(
                        category = categoryFilter,
                        cookingTool = cookingToolFilter,
                        timeFilter = timeFilter,
                        level = level ?: recipeDto.toDomainModel().level,
                        ingredientsQuery = ingredientsQuery,
                        useOnlySelected = useOnlySelected,
                    )

                val existingEntity = recipeDao.findExistingRecipeByTitle(domainRecipe.title, ingredientsQuery)

                val savedId =
                    if (existingEntity != null) {
                        existingEntity.id ?: throw IllegalStateException("ID missing")
                    } else {
                        recipeDao.insertRecipe(domainRecipe.toEntity())
                    }

                DataResult.Success(domainRecipe.copy(id = savedId))
            } catch (e: GeminiException) {
                // Gemini 관련 구체적 에러 매핑
                Log.e(TAG, "Gemini AI Error", e)
                val errorType =
                    when (e) {
                        is GeminiException.QuotaExceeded -> DataError.QUOTA_EXCEEDED
                        is GeminiException.ServerOverloaded -> DataError.SERVER_ERROR
                        is GeminiException.ApiKeyError -> DataError.API_KEY_ERROR
                        is GeminiException.ParsingError -> DataError.PARSING_ERROR
                        is GeminiException.NetworkError -> DataError.NETWORK_ERROR
                        is GeminiException.ResponseBlocked -> DataError.RESPONSE_BLOCKED
                        else -> DataError.UNKNOWN
                    }
                DataResult.Error(error = errorType, cause = e)
            } catch (e: Exception) {
                Log.e(TAG, "General Recipe Error", e)
                DataResult.Error(error = DataError.UNKNOWN, cause = e)
            }

        override fun getAllSavedRecipes(): Flow<List<Recipe>> =
            recipeDao
                .getAllRecipes()
                .map { entityList ->
                    entityList.map { it.toDomainModel() }
                }

        override suspend fun getSavedRecipeById(id: Long): DataResult<Recipe> =
            try {
                val entity = recipeDao.getRecipeById(id)
                if (entity != null) {
                    DataResult.Success(entity.toDomainModel())
                } else {
                    DataResult.Error(error = DataError.RECIPE_NOT_FOUND)
                }
            } catch (e: Exception) {
                DataResult.Error(error = DataError.UNKNOWN, cause = e)
            }

        override suspend fun findRecipesByFilters(
            ingredientsQuery: String,
            categoryFilter: RecipeCategoryType?,
            cookingToolFilter: CookingToolType?,
            timeFilter: String?,
            level: LevelType?,
            useOnlySelected: Boolean,
        ): DataResult<List<Recipe>> =
            try {
                val entities =
                    recipeDao.findRecipesByFilters(
                        ingredientsQuery = ingredientsQuery,
                        category = categoryFilter,
                        cookingTool = cookingToolFilter,
                        timeFilter = timeFilter,
                        level = level,
                        useOnlySelected = useOnlySelected,
                    )
                DataResult.Success(entities.map { it.toDomainModel() })
            } catch (e: Exception) {
                DataResult.Error(error = DataError.UNKNOWN, cause = e)
            }

        override suspend fun insertRecipe(recipe: Recipe): DataResult<Long> =
            try {
                val id = recipeDao.insertRecipe(recipe.toEntity())
                DataResult.Success(id)
            } catch (e: Exception) {
                DataResult.Error(error = DataError.SAVE_FAILED, cause = e)
            }

        override suspend fun updateRecipe(recipe: Recipe): DataResult<Unit> =
            try {
                recipeDao.updateRecipe(recipe.toEntity())
                DataResult.Success(Unit)
            } catch (e: Exception) {
                DataResult.Error(error = DataError.UPDATE_FAILED, cause = e)
            }

        override suspend fun deleteRecipe(recipe: Recipe): DataResult<Unit> =
            try {
                recipeDao.deleteRecipe(recipe.toEntity())
                DataResult.Success(Unit)
            } catch (e: Exception) {
                DataResult.Error(error = DataError.DELETE_FAILED, cause = e)
            }

        override suspend fun deleteAllRecipes(): DataResult<Unit> =
            try {
                recipeDao.deleteAllRecipes()
                DataResult.Success(Unit)
            } catch (e: Exception) {
                DataResult.Error(error = DataError.DELETE_FAILED, cause = e)
            }
    }
