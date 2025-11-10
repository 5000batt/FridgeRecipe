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
import kotlin.random.Random

@Database(entities = [IngredientEntity::class, RecipeEntity::class], version = 5, exportSchema = false)
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

/*val testDataList = listOf(
    Ingredient(id = 1L, name = "양파", amount = 3.0, unit = UnitType.COUNT, expirationDate = LocalDate.now().plusDays(0), storageLocation = StorageType.REFRIGERATED, category = CategoryType.VEGETABLE, emoticon = IngredientIcon.CARROT),
    Ingredient(id = 2L, name = "소고기", amount = 500.0, unit = UnitType.GRAM, expirationDate = LocalDate.now().plusDays(1), storageLocation = StorageType.REFRIGERATED, category = CategoryType.MEAT, emoticon = IngredientIcon.DEFAULT),
    Ingredient(id = 3L, name = "버섯", amount = 500.0, unit = UnitType.GRAM, expirationDate = LocalDate.now().plusDays(2), storageLocation = StorageType.REFRIGERATED, category = CategoryType.MEAT, emoticon = IngredientIcon.DEFAULT),
    Ingredient(id = 4L, name = "카레가루", amount = 500.0, unit = UnitType.GRAM, expirationDate = LocalDate.now().plusDays(10), storageLocation = StorageType.ROOM_TEMPERATURE, category = CategoryType.MEAT, emoticon = IngredientIcon.DEFAULT),
    Ingredient(id = 5L, name = "감자", amount = 500.0, unit = UnitType.GRAM, expirationDate = LocalDate.now().plusDays(-1), storageLocation = StorageType.ROOM_TEMPERATURE, category = CategoryType.MEAT, emoticon = IngredientIcon.DEFAULT),
    Ingredient(id = 6L, name = "긴재료이름테스트", amount = 500.0, unit = UnitType.GRAM, expirationDate = LocalDate.now().plusDays(5), storageLocation = StorageType.FROZEN, category = CategoryType.MEAT, emoticon = IngredientIcon.DEFAULT)
)*/

val testDataList: List<Ingredient> = buildList {
    val names = listOf("양파", "당근", "버섯", "계란", "소고기", "돼지고기", "우유", "치즈", "쌀", "파스타면")

    val storageTypes = StorageType.entries
    val categoryTypes = CategoryType.entries
    val unitTypes = UnitType.entries

    repeat(50) { index ->
        val name = names[index % names.size] + if (index >= names.size) " #${index / names.size + 1}" else ""

        val daysToExpiry = when (Random.nextInt(0, 10)) {
            in 0..1 -> Random.nextInt(-3, 0)
            in 2..4 -> Random.nextInt(0, 4)
            in 5..7 -> Random.nextInt(4, 15)
            else -> Random.nextInt(15, 60)
        }
        val expiryDate = LocalDate.now().plusDays(daysToExpiry.toLong())

        val storage = storageTypes.random()
        val category = categoryTypes.random()
        val unit = unitTypes.random()

        val amountValue = if (unit == UnitType.COUNT) Random.nextInt(1, 10).toDouble()
        else Random.nextDouble(100.0, 1000.0)

        val emoticon = if (name.contains("당근")) IngredientIcon.CARROT else IngredientIcon.DEFAULT

        add(
            Ingredient(
                id = (index + 1).toLong(),
                name = name,
                amount = amountValue,
                unit = unit,
                expirationDate = expiryDate,
                storageLocation = storage,
                category = category,
                emoticon = emoticon
            )
        )
    }
}