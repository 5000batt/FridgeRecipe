package com.kjw.fridgerecipe.di

import android.content.Context
import androidx.room.Room
import com.kjw.fridgerecipe.data.local.AppDatabase
import com.kjw.fridgerecipe.data.local.dao.IngredientDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fridge_recipe_db"
        )
        .build()
    }

    @Provides
    fun provideIngredientDao(appDatabase: AppDatabase): IngredientDao {
        return appDatabase.ingredientDao()
    }
}