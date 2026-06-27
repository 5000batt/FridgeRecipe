package com.kjw.fridgerecipe.presentation.viewmodel

import app.cash.turbine.test
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.IngredientCategoryType
import com.kjw.fridgerecipe.domain.model.IngredientIcon
import com.kjw.fridgerecipe.domain.model.StorageType
import com.kjw.fridgerecipe.domain.model.UnitType
import com.kjw.fridgerecipe.domain.usecase.DelIngredientUseCase
import com.kjw.fridgerecipe.domain.usecase.GetIngredientByIdUseCase
import com.kjw.fridgerecipe.domain.usecase.InsertIngredientUseCase
import com.kjw.fridgerecipe.domain.usecase.UpdateIngredientUseCase
import com.kjw.fridgerecipe.domain.util.DataError
import com.kjw.fridgerecipe.domain.util.DataResult
import com.kjw.fridgerecipe.presentation.mapper.IngredientUiMapper
import com.kjw.fridgerecipe.presentation.ui.model.IngredientValidationField
import com.kjw.fridgerecipe.presentation.util.MainDispatcherRule
import com.kjw.fridgerecipe.presentation.util.SnackbarType
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertIs

class IngredientEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getIngredientByIdUseCase: GetIngredientByIdUseCase = mockk()
    private val insertIngredientUseCase: InsertIngredientUseCase = mockk()
    private val updateIngredientUseCase: UpdateIngredientUseCase = mockk()
    private val delIngredientUseCase: DelIngredientUseCase = mockk()

    private lateinit var viewModel: IngredientEditViewModel

    private val fakeIngredient = Ingredient(
        id = 1L,
        name = "테스트 재료",
        amount = 1.0,
        unit = UnitType.COUNT,
        expirationDate = LocalDate.now().plusDays(7),
        storageLocation = StorageType.REFRIGERATED,
        emoticon = IngredientIcon.VEGETABLE,
        category = IngredientCategoryType.VEGETABLE,
    )

    @Before
    fun setup() {
        viewModel = IngredientEditViewModel(
            getIngredientByIdUseCase = getIngredientByIdUseCase,
            insertIngredientUseCase = insertIngredientUseCase,
            updateIngredientUseCase = updateIngredientUseCase,
            delIngredientUseCase = delIngredientUseCase,
            mapper = IngredientUiMapper(),
        )
    }

    // ── 저장 성공 ──────────────────────────────────────────────────────────

    @Test
    fun `신규 저장 성공 시 ShowSnackbar SUCCESS 후 NavigateBack 순서로 emit`() = runTest {
        coEvery { insertIngredientUseCase(any()) } returns DataResult.Success(Unit)

        viewModel.sideEffect.test {
            viewModel.onSaveOrUpdateIngredient(isEditMode = false)

            val first = awaitItem()
            assertIs<IngredientEditViewModel.IngredientEditSideEffect.ShowSnackbar>(first)
            assertEquals(SnackbarType.SUCCESS, first.type)

            assertIs<IngredientEditViewModel.IngredientEditSideEffect.NavigateBack>(awaitItem())
        }
    }

    @Test
    fun `수정 저장 성공 시 ShowSnackbar SUCCESS 후 NavigateBack 순서로 emit`() = runTest {
        coEvery { updateIngredientUseCase(any()) } returns DataResult.Success(Unit)

        viewModel.sideEffect.test {
            viewModel.onSaveOrUpdateIngredient(isEditMode = true)

            val first = awaitItem()
            assertIs<IngredientEditViewModel.IngredientEditSideEffect.ShowSnackbar>(first)
            assertEquals(SnackbarType.SUCCESS, first.type)

            assertIs<IngredientEditViewModel.IngredientEditSideEffect.NavigateBack>(awaitItem())
        }
    }

    // ── 유효성 오류 ────────────────────────────────────────────────────────

    @Test
    fun `이름 비어있음 오류 시 ScrollToField NAME emit`() = runTest {
        coEvery { insertIngredientUseCase(any()) } returns DataResult.Error(DataError.INGREDIENT_EMPTY_NAME)

        viewModel.sideEffect.test {
            viewModel.onSaveOrUpdateIngredient(isEditMode = false)

            val effect = awaitItem()
            assertIs<IngredientEditViewModel.IngredientEditSideEffect.ScrollToField>(effect)
            assertEquals(IngredientValidationField.NAME, effect.field)
        }
    }

    @Test
    fun `수량 오류 시 ScrollToField AMOUNT emit`() = runTest {
        coEvery { insertIngredientUseCase(any()) } returns DataResult.Error(DataError.INGREDIENT_INVALID_AMOUNT)

        viewModel.sideEffect.test {
            viewModel.onSaveOrUpdateIngredient(isEditMode = false)

            val effect = awaitItem()
            assertIs<IngredientEditViewModel.IngredientEditSideEffect.ScrollToField>(effect)
            assertEquals(IngredientValidationField.AMOUNT, effect.field)
        }
    }

    // ── DB 오류 ────────────────────────────────────────────────────────────

    @Test
    fun `DB 저장 실패 시 ShowSnackbar ERROR emit`() = runTest {
        coEvery { insertIngredientUseCase(any()) } returns DataResult.Error(DataError.SAVE_FAILED)

        viewModel.sideEffect.test {
            viewModel.onSaveOrUpdateIngredient(isEditMode = false)

            val effect = awaitItem()
            assertIs<IngredientEditViewModel.IngredientEditSideEffect.ShowSnackbar>(effect)
            assertEquals(SnackbarType.ERROR, effect.type)
        }
    }

    // ── 삭제 ───────────────────────────────────────────────────────────────

    @Test
    fun `삭제 성공 시 ShowSnackbar SUCCESS 후 NavigateBack 순서로 emit`() = runTest {
        coEvery { getIngredientByIdUseCase(any()) } returns DataResult.Success(fakeIngredient)
        coEvery { delIngredientUseCase(any()) } returns DataResult.Success(Unit)

        viewModel.loadIngredientById(1L)

        viewModel.sideEffect.test {
            viewModel.onDeleteIngredient()

            val first = awaitItem()
            assertIs<IngredientEditViewModel.IngredientEditSideEffect.ShowSnackbar>(first)
            assertEquals(SnackbarType.SUCCESS, first.type)

            assertIs<IngredientEditViewModel.IngredientEditSideEffect.NavigateBack>(awaitItem())
        }
    }

    @Test
    fun `삭제 DB 실패 시 ShowSnackbar ERROR emit`() = runTest {
        coEvery { getIngredientByIdUseCase(any()) } returns DataResult.Success(fakeIngredient)
        coEvery { delIngredientUseCase(any()) } returns DataResult.Error(DataError.DELETE_FAILED)

        viewModel.loadIngredientById(1L)

        viewModel.sideEffect.test {
            viewModel.onDeleteIngredient()

            val effect = awaitItem()
            assertIs<IngredientEditViewModel.IngredientEditSideEffect.ShowSnackbar>(effect)
            assertEquals(SnackbarType.ERROR, effect.type)
        }
    }
}
