package com.kjw.fridgerecipe.presentation.viewmodel

import android.net.Uri
import app.cash.turbine.test
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.usecase.DelRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipeByIdUseCase
import com.kjw.fridgerecipe.domain.usecase.InsertRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.SaveRecipeImageUseCase
import com.kjw.fridgerecipe.domain.usecase.UpdateRecipeUseCase
import com.kjw.fridgerecipe.domain.util.DataError
import com.kjw.fridgerecipe.domain.util.DataResult
import com.kjw.fridgerecipe.presentation.mapper.RecipeUiMapper
import com.kjw.fridgerecipe.presentation.ui.model.RecipeValidationField
import com.kjw.fridgerecipe.presentation.util.MainDispatcherRule
import com.kjw.fridgerecipe.presentation.util.SnackbarType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RecipeEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getSavedRecipeByIdUseCase: GetSavedRecipeByIdUseCase = mockk()
    private val insertRecipeUseCase: InsertRecipeUseCase = mockk()
    private val updateRecipeUseCase: UpdateRecipeUseCase = mockk()
    private val delRecipeUseCase: DelRecipeUseCase = mockk()
    private val saveRecipeImageUseCase: SaveRecipeImageUseCase = mockk()

    private lateinit var viewModel: RecipeEditViewModel

    private val fakeRecipe = Recipe(
        id = 1L,
        title = "테스트 레시피",
        servings = 2,
        time = 30,
        level = LevelType.BEGINNER,
        ingredients = emptyList(),
        steps = emptyList(),
    )

    @Before
    fun setup() {
        viewModel = RecipeEditViewModel(
            getSavedRecipeByIdUseCase = getSavedRecipeByIdUseCase,
            insertRecipeUseCase = insertRecipeUseCase,
            updateRecipeUseCase = updateRecipeUseCase,
            delRecipeUseCase = delRecipeUseCase,
            saveRecipeImageUseCase = saveRecipeImageUseCase,
            mapper = RecipeUiMapper(),
        )
    }

    // ── 저장 성공 ──────────────────────────────────────────────────────────

    @Test
    fun `신규 저장 성공 시 ShowSnackbar SUCCESS 후 NavigateBack 순서로 emit`() = runTest {
        coEvery { insertRecipeUseCase(any()) } returns DataResult.Success(1L)

        viewModel.sideEffect.test {
            viewModel.onSaveOrUpdateRecipe(isEditMode = false)

            val first = awaitItem()
            assertIs<RecipeEditViewModel.RecipeEditSideEffect.ShowSnackbar>(first)
            assertEquals(SnackbarType.SUCCESS, first.type)

            assertIs<RecipeEditViewModel.RecipeEditSideEffect.NavigateBack>(awaitItem())
        }
    }

    // ── 유효성 오류 ────────────────────────────────────────────────────────

    @Test
    fun `제목 오류 시 ScrollToField TITLE emit`() = runTest {
        coEvery { insertRecipeUseCase(any()) } returns DataResult.Error(DataError.RECIPE_EMPTY_TITLE)

        viewModel.sideEffect.test {
            viewModel.onSaveOrUpdateRecipe(isEditMode = false)

            val effect = awaitItem()
            assertIs<RecipeEditViewModel.RecipeEditSideEffect.ScrollToField>(effect)
            assertEquals(RecipeValidationField.TITLE, effect.field)
        }
    }

    @Test
    fun `재료 비어있음 오류 시 ScrollToField INGREDIENTS emit`() = runTest {
        coEvery { insertRecipeUseCase(any()) } returns DataResult.Error(DataError.RECIPE_EMPTY_INGREDIENTS)

        viewModel.sideEffect.test {
            viewModel.onSaveOrUpdateRecipe(isEditMode = false)

            val effect = awaitItem()
            assertIs<RecipeEditViewModel.RecipeEditSideEffect.ScrollToField>(effect)
            assertEquals(RecipeValidationField.INGREDIENTS, effect.field)
        }
    }

    // ── DB 오류 ────────────────────────────────────────────────────────────

    @Test
    fun `DB 저장 실패 시 ShowSnackbar ERROR emit`() = runTest {
        coEvery { insertRecipeUseCase(any()) } returns DataResult.Error(DataError.SAVE_FAILED)

        viewModel.sideEffect.test {
            viewModel.onSaveOrUpdateRecipe(isEditMode = false)

            val effect = awaitItem()
            assertIs<RecipeEditViewModel.RecipeEditSideEffect.ShowSnackbar>(effect)
            assertEquals(SnackbarType.ERROR, effect.type)
        }
    }

    // ── 삭제 ───────────────────────────────────────────────────────────────

    @Test
    fun `삭제 성공 시 ShowSnackbar SUCCESS 후 NavigateToList 순서로 emit`() = runTest {
        coEvery { getSavedRecipeByIdUseCase(any()) } returns DataResult.Success(fakeRecipe)
        coEvery { delRecipeUseCase(any()) } returns DataResult.Success(Unit)

        viewModel.loadRecipeForEdit(1L)

        viewModel.sideEffect.test {
            viewModel.onDeleteRecipe()

            val first = awaitItem()
            assertIs<RecipeEditViewModel.RecipeEditSideEffect.ShowSnackbar>(first)
            assertEquals(SnackbarType.SUCCESS, first.type)

            assertIs<RecipeEditViewModel.RecipeEditSideEffect.NavigateToList>(awaitItem())
        }
    }

    // ── 이미지 ─────────────────────────────────────────────────────────────

    @Test
    fun `이미지 저장 실패 시 ShowSnackbar ERROR emit`() = runTest {
        val mockUri = mockk<Uri>()
        every { mockUri.toString() } returns "content://test/image"
        coEvery { saveRecipeImageUseCase(any()) } returns DataResult.Error(DataError.SAVE_FAILED)

        viewModel.sideEffect.test {
            viewModel.onImageSelected(mockUri)

            val effect = awaitItem()
            assertIs<RecipeEditViewModel.RecipeEditSideEffect.ShowSnackbar>(effect)
            assertEquals(SnackbarType.ERROR, effect.type)
        }
    }
}
