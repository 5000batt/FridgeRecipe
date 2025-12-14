package com.kjw.fridgerecipe.data.repository

import com.kjw.fridgerecipe.data.local.dao.IngredientDao
import com.kjw.fridgerecipe.data.repository.mapper.toDomain
import com.kjw.fridgerecipe.data.repository.mapper.toEntity
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IngredientRepositoryImpl @Inject constructor(private val ingredientDao: IngredientDao): IngredientRepository {

    override suspend fun insertIngredient(ingredient: Ingredient) {
        ingredientDao.insertIngredient(ingredient.toEntity())
    }

    override suspend fun deleteIngredient(ingredient: Ingredient) {
        ingredientDao.deleteIngredient(ingredient.toEntity())
    }

    override fun getAllIngredients(): Flow<List<Ingredient>> {
        return ingredientDao.getAllIngredients().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAllIngredientsSuspend(): List<Ingredient> {
        return ingredientDao.getAllIngredientsSuspend().map { it.toDomain() }
    }

    override suspend fun getIngredientById(id: Long): Ingredient? {
        return ingredientDao.getIngredientById(id)?.toDomain()
    }

    override suspend fun updateIngredient(ingredient: Ingredient) {
        ingredientDao.updateIngredient(ingredient.toEntity())
    }

    override suspend fun deleteAllIngredients() {
        ingredientDao.deleteAllIngredients()
    }
}