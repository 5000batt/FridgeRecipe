package com.kjw.fridgerecipe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kjw.fridgerecipe.data.local.converter.LocalDateConverter
import com.kjw.fridgerecipe.data.local.converter.RecipeTypeConverters
import com.kjw.fridgerecipe.data.local.dao.IngredientDao
import com.kjw.fridgerecipe.data.local.dao.RecipeDao
import com.kjw.fridgerecipe.data.local.entity.IngredientEntity
import com.kjw.fridgerecipe.data.local.entity.RecipeEntity
import com.kjw.fridgerecipe.data.repository.mapper.toEntity
import com.kjw.fridgerecipe.di.ApplicationScope
import com.kjw.fridgerecipe.domain.model.CategoryType
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.IngredientIcon
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.domain.model.UnitType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@Database(entities = [IngredientEntity::class, RecipeEntity::class], version = 10, exportSchema = false)
@TypeConverters(LocalDateConverter::class, RecipeTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao

    class DatabaseCallback @Inject constructor(
        @ApplicationScope private val applicationScope: CoroutineScope,
        private val ingredientDaoProvider: dagger.Lazy<IngredientDao>
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            applicationScope.launch {
                val dao = ingredientDaoProvider.get()
                seedDatabase(dao)
            }
        }

        private suspend fun seedDatabase(dao: IngredientDao) {
            testDataList.map { it.toEntity() }.forEach { entity ->
                dao.insertIngredient(entity)
            }
        }
    }
}

val testDataList = listOf(
    Ingredient(id = 1L, name = "양파", amount = 3.0, unit = UnitType.COUNT, expirationDate = LocalDate.now().plusDays(0), storageLocation = StorageType.REFRIGERATED, category = CategoryType.VEGETABLE, emoticon = IngredientIcon.ONION),
    Ingredient(id = 2L, name = "소고기", amount = 500.0, unit = UnitType.GRAM, expirationDate = LocalDate.now().plusDays(1), storageLocation = StorageType.REFRIGERATED, category = CategoryType.MEAT, emoticon = IngredientIcon.BEEF),
    Ingredient(id = 3L, name = "버섯", amount = 500.0, unit = UnitType.GRAM, expirationDate = LocalDate.now().plusDays(2), storageLocation = StorageType.REFRIGERATED, category = CategoryType.VEGETABLE, emoticon = IngredientIcon.MUSHROOM),
    Ingredient(id = 4L, name = "카레가루", amount = 500.0, unit = UnitType.GRAM, expirationDate = LocalDate.now().plusDays(10), storageLocation = StorageType.ROOM_TEMPERATURE, category = CategoryType.MEAT, emoticon = IngredientIcon.DEFAULT),
    Ingredient(id = 5L, name = "감자", amount = 500.0, unit = UnitType.GRAM, expirationDate = LocalDate.now().plusDays(-1), storageLocation = StorageType.ROOM_TEMPERATURE, category = CategoryType.VEGETABLE, emoticon = IngredientIcon.POTATO),
)
