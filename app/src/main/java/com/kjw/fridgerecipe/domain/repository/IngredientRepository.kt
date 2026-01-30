package com.kjw.fridgerecipe.domain.repository

import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.util.DataResult
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {

    suspend fun insertIngredient(ingredient: Ingredient): DataResult<Unit>

    suspend fun deleteIngredient(ingredient: Ingredient): DataResult<Unit>

    fun getAllIngredients(): Flow<List<Ingredient>>

    suspend fun getIngredientById(id: Long): DataResult<Ingredient>

    suspend fun updateIngredient(ingredient: Ingredient): DataResult<Unit>

    suspend fun getAllIngredientsSuspend(): DataResult<List<Ingredient>>

    suspend fun deleteAllIngredients(): DataResult<Unit>
}
