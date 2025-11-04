package com.kjw.fridgerecipe.data.repository

import com.kjw.fridgerecipe.data.local.dao.IngredientDao
import com.kjw.fridgerecipe.data.repository.mapper.toDomain
import com.kjw.fridgerecipe.data.repository.mapper.toEntity
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IngredientRepositoryImpl @Inject constructor(private val dao: IngredientDao): IngredientRepository {

    override suspend fun insertIngredient(ingredient: Ingredient) {
        dao.insertIngredient(ingredient.toEntity())
    }

    override suspend fun deleteIngredient(ingredient: Ingredient) {
        dao.deleteIngredient(ingredient.toEntity())
    }

    override fun getAllIngredients(): Flow<List<Ingredient>> {
        return dao.getAllIngredients().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getIngredientById(id: Long): Ingredient? {
        return dao.getIngredientById(id)?.toDomain()
    }

    override suspend fun updateIngredient(ingredient: Ingredient) {
        dao.updateIngredient(ingredient.toEntity())
    }
}