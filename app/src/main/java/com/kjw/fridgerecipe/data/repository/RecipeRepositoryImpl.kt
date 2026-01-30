package com.kjw.fridgerecipe.data.repository

import android.util.Log
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.data.datasource.RecipeRemoteDataSource
import com.kjw.fridgerecipe.data.local.dao.RecipeDao
import com.kjw.fridgerecipe.data.repository.mapper.toDomainModel
import com.kjw.fridgerecipe.data.repository.mapper.toEntity
import com.kjw.fridgerecipe.domain.model.CookingToolType
import com.kjw.fridgerecipe.domain.model.GeminiException
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import com.kjw.fridgerecipe.domain.util.DataResult
import com.kjw.fridgerecipe.presentation.util.RecipeConstants.FILTER_ANY
import com.kjw.fridgerecipe.presentation.util.UiText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val recipeDao: RecipeDao,
    private val remoteDataSource: RecipeRemoteDataSource
) : RecipeRepository {

    private companion object {
        const val TAG = "RecipeRepo"
    }

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
    ): DataResult<Recipe> {
        return try {
            val safeTime = sanitizeFilter(timeFilter)

            val recipeDto = remoteDataSource.getAiRecipe(
                ingredients = ingredients,
                timeFilter = safeTime,
                level = level,
                categoryFilter = categoryFilter,
                cookingToolFilter = cookingToolFilter,
                useOnlySelected = useOnlySelected,
                excludedIngredients = excludedIngredients
            )

            val domainRecipe = recipeDto.toDomainModel().copy(
                category = categoryFilter,
                cookingTool = cookingToolFilter,
                timeFilter = safeTime,
                level = level ?: recipeDto.toDomainModel().level,
                ingredientsQuery = ingredientsQuery,
                useOnlySelected = useOnlySelected
            )

            val existingEntity = recipeDao.findExistingRecipeByTitle(domainRecipe.title, ingredientsQuery)

            val savedId = if (existingEntity != null) {
                existingEntity.id ?: throw IllegalStateException("ID missing")
            } else {
                recipeDao.insertRecipe(domainRecipe.toEntity())
            }

            DataResult.Success(domainRecipe.copy(id = savedId))
        } catch (e: GeminiException) {
            // Gemini 관련 구체적 에러 매핑
            Log.e(TAG, "Gemini AI Error", e)
            val (titleRes, messageRes) = when (e) {
                is GeminiException.QuotaExceeded -> 
                    R.string.error_title_quota to R.string.error_msg_quota
                is GeminiException.ServerOverloaded -> 
                    R.string.error_title_server to R.string.error_msg_server
                is GeminiException.ApiKeyError -> 
                    R.string.error_title_update to R.string.error_msg_update
                is GeminiException.ParsingError -> 
                    R.string.error_title_parsing to R.string.error_msg_parsing
                is GeminiException.NetworkError -> 
                    R.string.error_title_network to R.string.error_msg_network
                is GeminiException.ResponseBlocked -> 
                    R.string.error_title_blocked to R.string.error_msg_blocked
                else -> 
                    R.string.error_title_generic to R.string.error_msg_generic
            }
            DataResult.Error(
                message = UiText.StringResource(messageRes),
                title = UiText.StringResource(titleRes),
                cause = e
            )
        } catch (e: Exception) {
            Log.e(TAG, "General Recipe Error", e)
            DataResult.Error(
                message = UiText.StringResource(R.string.error_msg_recipe_fetch_failed),
                title = UiText.StringResource(R.string.error_title_generic),
                cause = e
            )
        }
    }

    override fun getAllSavedRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes()
            .map { entityList ->
                entityList.map { it.toDomainModel() }
            }
    }

    override suspend fun getSavedRecipeById(id: Long): DataResult<Recipe> {
        return try {
            val entity = recipeDao.getRecipeById(id)
            if (entity != null) {
                DataResult.Success(entity.toDomainModel())
            } else {
                DataResult.Error(message = UiText.StringResource(R.string.error_recipe_not_found))
            }
        } catch (e: Exception) {
            DataResult.Error(message = UiText.StringResource(R.string.error_msg_generic), cause = e)
        }
    }

    override suspend fun findRecipesByFilters(
        ingredientsQuery: String,
        categoryFilter: RecipeCategoryType?,
        cookingToolFilter: CookingToolType?,
        timeFilter: String?,
        level: LevelType?,
        useOnlySelected: Boolean
    ): DataResult<List<Recipe>> {
        return try {
            val entities = recipeDao.findRecipesByFilters(
                ingredientsQuery = ingredientsQuery,
                category = categoryFilter,
                cookingTool = cookingToolFilter,
                timeFilter = sanitizeFilter(timeFilter),
                level = level,
                useOnlySelected = useOnlySelected
            )
            DataResult.Success(entities.map { it.toDomainModel() })
        } catch (e: Exception) {
            DataResult.Error(message = UiText.StringResource(R.string.error_msg_generic), cause = e)
        }
    }

    override suspend fun insertRecipe(recipe: Recipe): DataResult<Long> {
        return try {
            val id = recipeDao.insertRecipe(recipe.toEntity())
            DataResult.Success(id)
        } catch (e: Exception) {
            DataResult.Error(message = UiText.StringResource(R.string.error_save_failed), cause = e)
        }
    }

    override suspend fun updateRecipe(recipe: Recipe): DataResult<Unit> {
        return try {
            recipeDao.updateRecipe(recipe.toEntity())
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(message = UiText.StringResource(R.string.error_update_failed), cause = e)
        }
    }

    override suspend fun deleteRecipe(recipe: Recipe): DataResult<Unit> {
        return try {
            recipeDao.deleteRecipe(recipe.toEntity())
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(message = UiText.StringResource(R.string.error_delete_failed), cause = e)
        }
    }

    override suspend fun deleteAllRecipes(): DataResult<Unit> {
        return try {
            recipeDao.deleteAllRecipes()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(message = UiText.StringResource(R.string.error_msg_generic), cause = e)
        }
    }
}
