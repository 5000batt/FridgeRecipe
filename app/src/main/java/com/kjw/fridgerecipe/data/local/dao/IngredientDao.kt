package com.kjw.fridgerecipe.data.local.dao

import androidx.room.*
import com.kjw.fridgerecipe.data.local.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(entity: IngredientEntity)

    @Delete
    suspend fun deleteIngredient(entity: IngredientEntity)

    @Query("SELECT * FROM ingredients ORDER BY expirationDate ASC")
    fun getAllIngredients(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients")
    suspend fun getAllIngredientsSuspend(): List<IngredientEntity>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getIngredientById(id: Long): IngredientEntity?

    @Update
    suspend fun updateIngredient(entity: IngredientEntity)

    @Query("DELETE FROM ingredients")
    suspend fun deleteAllIngredients()
}