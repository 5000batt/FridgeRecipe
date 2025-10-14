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

}