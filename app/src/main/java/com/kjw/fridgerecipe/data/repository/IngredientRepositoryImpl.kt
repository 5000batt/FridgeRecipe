package com.kjw.fridgerecipe.data.repository

import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.data.local.dao.IngredientDao
import com.kjw.fridgerecipe.data.repository.mapper.toDomainModel
import com.kjw.fridgerecipe.data.repository.mapper.toEntity
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import com.kjw.fridgerecipe.domain.util.DataResult
import com.kjw.fridgerecipe.presentation.util.UiText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IngredientRepositoryImpl @Inject constructor(
    private val ingredientDao: IngredientDao
) : IngredientRepository {

    override suspend fun insertIngredient(ingredient: Ingredient): DataResult<Unit> {
        return try {
            ingredientDao.insertIngredient(ingredient.toEntity())
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(message = UiText.StringResource(R.string.error_save_failed), cause = e)
        }
    }

    override suspend fun deleteIngredient(ingredient: Ingredient): DataResult<Unit> {
        return try {
            ingredientDao.deleteIngredient(ingredient.toEntity())
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(message = UiText.StringResource(R.string.error_delete_failed), cause = e)
        }
    }

    override fun getAllIngredients(): Flow<List<Ingredient>> {
        return ingredientDao.getAllIngredients().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getAllIngredientsSuspend(): DataResult<List<Ingredient>> {
        return try {
            val entities = ingredientDao.getAllIngredientsSuspend()
            DataResult.Success(entities.map { it.toDomainModel() })
        } catch (e: Exception) {
            DataResult.Error(message = UiText.StringResource(R.string.error_msg_generic), cause = e)
        }
    }

    override suspend fun getIngredientById(id: Long): DataResult<Ingredient> {
        return try {
            val entity = ingredientDao.getIngredientById(id)
            if (entity != null) {
                DataResult.Success(entity.toDomainModel())
            } else {
                DataResult.Error(message = UiText.StringResource(R.string.error_msg_generic))
            }
        } catch (e: Exception) {
            DataResult.Error(message = UiText.StringResource(R.string.error_msg_generic), cause = e)
        }
    }

    override suspend fun updateIngredient(ingredient: Ingredient): DataResult<Unit> {
        return try {
            ingredientDao.updateIngredient(ingredient.toEntity())
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(message = UiText.StringResource(R.string.error_update_failed), cause = e)
        }
    }

    override suspend fun deleteAllIngredients(): DataResult<Unit> {
        return try {
            ingredientDao.deleteAllIngredients()
            DataResult.Success(Unit)
        } catch (e: Exception) {
            DataResult.Error(message = UiText.StringResource(R.string.error_msg_generic), cause = e)
        }
    }
}
