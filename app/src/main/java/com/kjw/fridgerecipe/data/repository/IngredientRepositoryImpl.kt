package com.kjw.fridgerecipe.data.repository

import com.kjw.fridgerecipe.data.local.dao.IngredientDao
import com.kjw.fridgerecipe.data.repository.mapper.toDomainModel
import com.kjw.fridgerecipe.data.repository.mapper.toEntity
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import com.kjw.fridgerecipe.domain.util.DataError
import com.kjw.fridgerecipe.domain.util.DataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IngredientRepositoryImpl
    @Inject
    constructor(
        private val ingredientDao: IngredientDao,
    ) : IngredientRepository {
        override suspend fun insertIngredient(ingredient: Ingredient): DataResult<Unit> =
            try {
                ingredientDao.insertIngredient(ingredient.toEntity())
                DataResult.Success(Unit)
            } catch (e: Exception) {
                DataResult.Error(error = DataError.SAVE_FAILED, cause = e)
            }

        override suspend fun deleteIngredient(ingredient: Ingredient): DataResult<Unit> =
            try {
                ingredientDao.deleteIngredient(ingredient.toEntity())
                DataResult.Success(Unit)
            } catch (e: Exception) {
                DataResult.Error(error = DataError.DELETE_FAILED, cause = e)
            }

        override fun getAllIngredients(): Flow<List<Ingredient>> =
            ingredientDao.getAllIngredients().map { entities ->
                entities.map { it.toDomainModel() }
            }

        override suspend fun getAllIngredientsSuspend(): DataResult<List<Ingredient>> =
            try {
                val entities = ingredientDao.getAllIngredientsSuspend()
                DataResult.Success(entities.map { it.toDomainModel() })
            } catch (e: Exception) {
                DataResult.Error(error = DataError.UNKNOWN, cause = e)
            }

        override suspend fun getIngredientById(id: Long): DataResult<Ingredient> =
            try {
                val entity = ingredientDao.getIngredientById(id)
                if (entity != null) {
                    DataResult.Success(entity.toDomainModel())
                } else {
                    DataResult.Error(error = DataError.UNKNOWN)
                }
            } catch (e: Exception) {
                DataResult.Error(error = DataError.UNKNOWN, cause = e)
            }

        override suspend fun updateIngredient(ingredient: Ingredient): DataResult<Unit> =
            try {
                ingredientDao.updateIngredient(ingredient.toEntity())
                DataResult.Success(Unit)
            } catch (e: Exception) {
                DataResult.Error(error = DataError.UPDATE_FAILED, cause = e)
            }

        override suspend fun deleteAllIngredients(): DataResult<Unit> =
            try {
                ingredientDao.deleteAllIngredients()
                DataResult.Success(Unit)
            } catch (e: Exception) {
                DataResult.Error(error = DataError.DELETE_FAILED, cause = e)
            }
    }
