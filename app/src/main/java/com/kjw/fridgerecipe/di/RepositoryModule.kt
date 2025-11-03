package com.kjw.fridgerecipe.di

import com.kjw.fridgerecipe.data.repository.IngredientRepositoryImpl
import com.kjw.fridgerecipe.data.repository.RecipeRepositoryImpl
import com.kjw.fridgerecipe.data.repository.TestRecipeRepository
import com.kjw.fridgerecipe.domain.repository.IngredientRepository
import com.kjw.fridgerecipe.domain.repository.RecipeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindIngredientRepository(
        implementation: IngredientRepositoryImpl
    ): IngredientRepository

    /*@Binds
    @Singleton
    abstract fun bindTestRecipeRepository(
        implementation: TestRecipeRepository
    ): RecipeRepository*/

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(
        implementation: RecipeRepositoryImpl
    ): RecipeRepository
}