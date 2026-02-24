package com.kjw.fridgerecipe.di

import com.kjw.fridgerecipe.data.datasource.RecipeRemoteDataSource
import com.kjw.fridgerecipe.data.datasource.RecipeRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    @Singleton
    abstract fun bindRecipeRemoteDataSource(impl: RecipeRemoteDataSourceImpl): RecipeRemoteDataSource
}
