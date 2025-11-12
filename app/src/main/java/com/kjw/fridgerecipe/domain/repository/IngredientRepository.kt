package com.kjw.fridgerecipe.domain.repository

import com.kjw.fridgerecipe.domain.model.Ingredient
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {

    suspend fun insertIngredient(ingredient: Ingredient)

    suspend fun deleteIngredient(ingredient: Ingredient)

    fun getAllIngredients(): Flow<List<Ingredient>>

    suspend fun getIngredientById(id: Long): Ingredient?

    suspend fun updateIngredient(ingredient: Ingredient)

    suspend fun getAllIngredientsSuspend(): List<Ingredient>
}