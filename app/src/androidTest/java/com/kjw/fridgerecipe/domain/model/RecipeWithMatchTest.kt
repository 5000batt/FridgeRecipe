package com.kjw.fridgerecipe.domain.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kjw.fridgerecipe.domain.util.RecipeMatcher
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecipeWithMatchTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var recipeMatcher: RecipeMatcher

    @Before
    fun setup() {
        hiltRule.inject()
    }

    private fun createIngredient(name: String) =
        Ingredient(
            id = 1L,
            name = name,
            amount = 1.0,
            unit = UnitType.COUNT,
            expirationDate = LocalDate.now().plusDays(7),
            storageLocation = StorageType.REFRIGERATED,
            emoticon = IngredientIcon.VEGETABLE,
            category = IngredientCategoryType.VEGETABLE,
        )

    private fun createRecipe(vararg ingredientNames: String) =
        Recipe(
            id = 1L,
            title = "테스트 레시피",
            servings = 1,
            time = 20,
            level = LevelType.BEGINNER,
            ingredients =
                ingredientNames.map {
                    RecipeIngredient(name = it, quantity = "약간", isEssential = true)
                },
            steps = emptyList(),
        )

    @Test
    fun testBasicMatch() =
        runBlocking {
            val fridge = listOf(createIngredient("양파"))
            val recipe = createRecipe("양파")

            val result = recipeMatcher.calculateMatch(recipe, fridge)

            assertTrue(result.isCookable)
            assertEquals(1, result.matchCount)
        }

    @Test
    fun testNounExtractionMatch() =
        runBlocking {
            val fridge = listOf(createIngredient("양파"))
            val recipe = createRecipe("다진 양파")

            val result = recipeMatcher.calculateMatch(recipe, fridge)

            assertTrue("수식어가 있어도 명사가 같으면 매칭되어야 함", result.isCookable)
        }

    @Test
    fun testFalsePositiveSuffix() =
        runBlocking {
            val fridge = listOf(createIngredient("고추"))
            val recipe = createRecipe("고추장")

            val result = recipeMatcher.calculateMatch(recipe, fridge)

            assertFalse("고추장 레시피에 고추만 있으면 요리 불가여야 함", result.isCookable)
        }

    @Test
    fun testSynonymMatch() =
        runBlocking {
            val fridge = listOf(createIngredient("계란"))
            val recipe = createRecipe("달걀")

            val result = recipeMatcher.calculateMatch(recipe, fridge)

            assertTrue("계란과 달걀은 동의어로 매칭되어야 함", result.isCookable)
        }
}
