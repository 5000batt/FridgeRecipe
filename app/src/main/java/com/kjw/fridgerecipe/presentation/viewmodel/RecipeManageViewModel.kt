package com.kjw.fridgerecipe.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.model.RecipeIngredient
import com.kjw.fridgerecipe.domain.model.RecipeStep
import com.kjw.fridgerecipe.domain.usecase.DelRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipeByIdUseCase
import com.kjw.fridgerecipe.domain.usecase.InsertRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.UpdateRecipeUseCase
import com.kjw.fridgerecipe.presentation.ui.model.OperationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.UUID
import javax.inject.Inject
import androidx.core.graphics.scale
import com.kjw.fridgerecipe.domain.model.RecipeSearchMetadata

enum class RecipeValidationField {
    TITLE, SERVINGS, TIME, INGREDIENTS, STEPS
}

enum class ListErrorType {
    NONE,
    IS_EMPTY,
    HAS_BLANK_ITEMS
}
data class IngredientUiState(
    val name: String,
    val quantity: String,
    val isEssential: Boolean
)

data class StepUiState(
    val number: Int,
    val description: String
)

data class RecipeEditUiState(
    val title: String = "",
    val titleError: String? = null,
    val servingsState: String = "",
    val servingsError: String? = null,
    val timeState: String = "",
    val timeError: String? = null,
    val level: LevelType = LevelType.ETC,
    val categoryState: String = "상관없음",
    val utensilState: String = "상관없음",
    val ingredientsState: List<IngredientUiState> = emptyList(),
    val ingredientsError: String? = null,
    val ingredientsErrorType: ListErrorType = ListErrorType.NONE,
    val stepsState: List<StepUiState> = emptyList(),
    val stepsError: String? = null,
    val stepsErrorType: ListErrorType = ListErrorType.NONE,
    val showDeleteDialog: Boolean = false,
    val selectedRecipeTitle: String? = null,
    val imageUri: String? = null
)

@HiltViewModel
class RecipeManageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSavedRecipeByIdUseCase: GetSavedRecipeByIdUseCase,
    private val insertRecipeUseCase: InsertRecipeUseCase,
    private val updateRecipeUseCase: UpdateRecipeUseCase,
    private val delRecipeUseCase: DelRecipeUseCase
) : ViewModel() {

    sealed class NavigationEvent {
        data object NavigateBack : NavigationEvent()
        data object NavigateToList : NavigationEvent()
    }

    private val _operationResultEvent = MutableSharedFlow<OperationResult>()
    val operationResultEvent: SharedFlow<OperationResult> = _operationResultEvent.asSharedFlow()
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()
    private val _validationEvent = MutableSharedFlow<RecipeValidationField>()
    val validationEvent: SharedFlow<RecipeValidationField> = _validationEvent.asSharedFlow()

    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()
    private val _editUiState = MutableStateFlow(RecipeEditUiState())
    val editUiState: StateFlow<RecipeEditUiState> = _editUiState.asStateFlow()

    fun loadRecipeById(id: Long) {
        viewModelScope.launch {
            val recipe = getSavedRecipeByIdUseCase(id)
            _selectedRecipe.value = recipe
            _editUiState.value = recipe?.toEditUiState() ?: RecipeEditUiState()
        }
    }

    fun clearSelectedRecipe() {
        _selectedRecipe.value = null
        _editUiState.value = RecipeEditUiState()
    }

    fun onSaveOrUpdateRecipe(isEditMode: Boolean) {
        viewModelScope.launch {
            if (!validateInputs()) {
                return@launch
            }

            val recipeToSave = buildRecipeFromState(isEditMode)

            val success = if (isEditMode) {
                updateRecipeUseCase(recipeToSave)
            } else {
                insertRecipeUseCase(recipeToSave)
            }

            if (success) {
                val message = if (isEditMode) "수정되었습니다." else "저장되었습니다."
                _operationResultEvent.emit(OperationResult.Success(message))
                _navigationEvent.emit(NavigationEvent.NavigateBack)
            } else {
                val message = if (isEditMode) "수정에 실패했습니다." else "저장에 실패했습니다."
                _operationResultEvent.emit(OperationResult.Failure(message))
            }
        }
    }

    fun onDeleteRecipe() {
        viewModelScope.launch {
            _selectedRecipe.value?.let {
                val success = delRecipeUseCase(it)
                if (success) {
                    _operationResultEvent.emit(OperationResult.Success("삭제되었습니다."))
                    _navigationEvent.emit(NavigationEvent.NavigateToList)
                } else {
                    _operationResultEvent.emit(OperationResult.Failure("삭제에 실패했습니다."))
                }
            }
            _editUiState.update { it.copy(showDeleteDialog = false) }
        }
    }

    fun onTitleChanged(newTitle: String) {
        _editUiState.update { it.copy(title = newTitle, titleError = null) }
    }

    fun onServingsChanged(newServings: String) {
        if (newServings.length <= 3 && newServings.all { it.isDigit() }) {
            _editUiState.update { it.copy(servingsState = newServings, servingsError = null) }
        }
    }

    fun onTimeChanged(newTime: String) {
        if (newTime.length <= 3 && newTime.all { it.isDigit() }) {
            _editUiState.update { it.copy(timeState = newTime, timeError = null) }
        }
    }

    fun onLevelChanged(newLevel: LevelType) {
        _editUiState.update { it.copy(level = newLevel) }
    }

    fun onCategoryChanged(newCategory: String) {
        _editUiState.update { it.copy(categoryState = newCategory) }
    }

    fun onUtensilChanged(newUtensil: String) {
        _editUiState.update { it.copy(utensilState = newUtensil) }
    }

    fun onAddIngredient() {
        _editUiState.update { currentState ->
            val newList = currentState.ingredientsState + IngredientUiState("", "", false)
            val (newError, newType) = if (currentState.ingredientsErrorType == ListErrorType.IS_EMPTY) {
                Pair(null, ListErrorType.NONE)
            } else {
                Pair(currentState.ingredientsError, currentState.ingredientsErrorType)
            }
            currentState.copy(
                ingredientsState = newList,
                ingredientsError = newError,
                ingredientsErrorType = newType
            )
        }
    }

    fun onRemoveIngredient(index: Int) {
        _editUiState.update { currentState ->
            val newList = currentState.ingredientsState.toMutableList().apply { removeAt(index) }
            val (newError, newType) = checkIngredientErrors(newList)
            currentState.copy(
                ingredientsState = newList,
                ingredientsError = newError,
                ingredientsErrorType = newType
            )
        }
    }

    fun onIngredientNameChanged(index: Int, newName: String) {
        _editUiState.update { currentState ->
            val newList = currentState.ingredientsState.mapIndexed { i, item ->
                if (i == index) item.copy(name = newName) else item
            }
            val (newError, newType) = checkIngredientErrors(newList)
            currentState.copy(
                ingredientsState = newList,
                ingredientsError = newError,
                ingredientsErrorType = newType
            )
        }
    }

    fun onIngredientQuantityChanged(index: Int, newQty: String) {
        _editUiState.update { currentState ->
            val newList = currentState.ingredientsState.mapIndexed { i, item ->
                if (i == index) item.copy(quantity = newQty) else item
            }
            val (newError, newType) = checkIngredientErrors(newList)
            currentState.copy(
                ingredientsState = newList,
                ingredientsError = newError,
                ingredientsErrorType = newType
            )
        }
    }

    fun onIngredientEssentialChanged(index: Int, isChecked: Boolean) {
        _editUiState.update { currentState ->
            val newList = currentState.ingredientsState.mapIndexed { i, item ->
                if (i == index) item.copy(isEssential = isChecked) else item
            }
            currentState.copy(ingredientsState = newList)
        }
    }

    fun onAddStep() {
        _editUiState.update { currentState ->
            val newList = currentState.stepsState + StepUiState(currentState.stepsState.size + 1, "")
            val (newError, newType) = if (currentState.stepsErrorType == ListErrorType.IS_EMPTY) {
                Pair(null, ListErrorType.NONE)
            } else {
                Pair(currentState.stepsError, currentState.stepsErrorType)
            }
            currentState.copy(
                stepsState = newList,
                stepsError = newError,
                stepsErrorType = newType
            )
        }
    }

    fun onRemoveStep(index: Int) {
        _editUiState.update { currentState ->
            val newList = currentState.stepsState.toMutableList().apply { removeAt(index) }
            val (newError, newType) = checkStepErrors(newList)
            currentState.copy(
                stepsState = newList,
                stepsError = newError,
                stepsErrorType = newType
            )
        }
    }

    fun onStepDescriptionChanged(index: Int, newDesc: String) {
        _editUiState.update { currentState ->
            val newList = currentState.stepsState.mapIndexed { i, item ->
                if (i == index) item.copy(number = i + 1, description = newDesc) else item
            }
            val (newError, newType) = checkStepErrors(newList)
            currentState.copy(
                stepsState = newList,
                stepsError = newError,
                stepsErrorType = newType
            )
        }
    }

    fun onDeleteDialogShow() {
        _editUiState.update { it.copy(showDeleteDialog = true, selectedRecipeTitle = _selectedRecipe.value?.title) }
    }

    fun onDeleteDialogDismiss() {
        _editUiState.update { it.copy(showDeleteDialog = false) }
    }

    fun onImageSelected(uri: Uri?) {
        uri?.let {
            val savedUri = saveImageToInternalStorage(it)
            _editUiState.update { state -> state.copy(imageUri = savedUri) }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, 1024, 1024)
            options.inJustDecodeBounds = false

            val scaledBitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            } ?: return null

            val directory = File(context.filesDir, "recipe_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val fileName = "IMG_${UUID.randomUUID()}.jpg"
            val file = File(directory, fileName)
            val outputStream = FileOutputStream(file)

            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)

            outputStream.flush()
            outputStream.close()

            scaledBitmap.recycle()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /*private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        if (originalWidth <= maxDimension && originalHeight <= maxDimension) {
            return bitmap
        }

        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()
        var newWidth = 0
        var newHeight = 0

        if (originalWidth > originalHeight) {
            newWidth = maxDimension
            newHeight = (newWidth / aspectRatio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (newHeight * aspectRatio).toInt()
        }

        return bitmap.scale(newWidth, newHeight)
    }*/

    private fun checkIngredientErrors(list: List<IngredientUiState>): Pair<String?, ListErrorType> {
        return if (!list.any { it.name.isBlank() || it.quantity.isBlank() }) {
            Pair(null, ListErrorType.NONE)
        } else {
            Pair(_editUiState.value.ingredientsError, _editUiState.value.ingredientsErrorType)
        }
    }

    private fun checkStepErrors(list: List<StepUiState>): Pair<String?, ListErrorType> {
        return if (!list.any { it.description.isBlank() }) {
            Pair(null, ListErrorType.NONE)
        } else {
            Pair(_editUiState.value.stepsError, _editUiState.value.stepsErrorType)
        }
    }

    private suspend fun validateInputs(): Boolean {
        val currentState = _editUiState.value

        if (currentState.title.isBlank()) {
            _editUiState.update { it.copy(titleError = "레시피 이름을 입력해주세요.") }
            _validationEvent.emit(RecipeValidationField.TITLE)
            return false
        }

        if (currentState.servingsState.isBlank()) {
            _editUiState.update { it.copy(servingsError = "조리 양을 입력해주세요.") }
            _validationEvent.emit(RecipeValidationField.SERVINGS)
            return false
        }

        if (currentState.timeState.isBlank()) {
            _editUiState.update { it.copy(timeError = "조리 시간을 입력해주세요.") }
            _validationEvent.emit(RecipeValidationField.TIME)
            return false
        }

        if (currentState.ingredientsState.isEmpty()) {
            _editUiState.update { it.copy(ingredientsError = "재료를 추가해주세요.", ingredientsErrorType = ListErrorType.IS_EMPTY) }
            _validationEvent.emit(RecipeValidationField.INGREDIENTS)
            return false
        } else if (currentState.ingredientsState.any { it.name.isBlank() || it.quantity.isBlank() }) {
            _editUiState.update { it.copy(ingredientsError = "내용이 비어있는 재료가 있습니다.", ingredientsErrorType = ListErrorType.HAS_BLANK_ITEMS) }
            _validationEvent.emit(RecipeValidationField.INGREDIENTS)
            return false
        }

        if (currentState.stepsState.isEmpty()) {
            _editUiState.update { it.copy(stepsError = "조리 순서를 추가해주세요.", stepsErrorType = ListErrorType.IS_EMPTY) }
            _validationEvent.emit(RecipeValidationField.STEPS)
            return false
        } else if (currentState.stepsState.any { it.description.isBlank() }) {
            _editUiState.update { it.copy(stepsError = "내용이 비어있는 조리 순서가 있습니다.", stepsErrorType = ListErrorType.HAS_BLANK_ITEMS) }
            _validationEvent.emit(RecipeValidationField.STEPS)
            return false
        }

        return true
    }

    private fun buildRecipeFromState(isEditMode: Boolean): Recipe {
        val currentState = _editUiState.value
        val actualTimeInt = currentState.timeState.toIntOrNull() ?: 0
        val actualLevel = currentState.level
        val actualCategory = if (currentState.categoryState == "상관없음") null else currentState.categoryState
        val actualUtensil = if (currentState.utensilState == "상관없음") null else currentState.utensilState

        val timeFilterTag = when {
            actualTimeInt <= 15 -> "15분 이내"
            actualTimeInt <= 30 -> "30분 이내"
            actualTimeInt <= 60 -> "60분 이내"
            else -> null
        }

        val recipeId = if (isEditMode) _selectedRecipe.value?.id else null
        val ingredientsQueryTag = currentState.ingredientsState
            .filter { it.isEssential }
            .map { it.name }
            .sorted()
            .joinToString(",")

        val metadata = RecipeSearchMetadata(
            ingredientsQuery = ingredientsQueryTag,
            timeFilter = timeFilterTag,
            levelFilter = actualLevel,
            categoryFilter = actualCategory,
            utensilFilter = actualUtensil,
            useOnlySelected = false
        )

        return Recipe(
            id = recipeId,
            title = currentState.title.trim(),
            servings = "${currentState.servingsState}인분",
            time = "${currentState.timeState}분",
            level = actualLevel,
            ingredients = currentState.ingredientsState.map {
                RecipeIngredient(
                    name = it.name,
                    quantity = it.quantity,
                    isEssential = it.isEssential
                )
            },
            steps = currentState.stepsState.map { RecipeStep(it.number, it.description) },
            searchMetadata = metadata,
            imageUri = currentState.imageUri
        )
    }
}

private fun Recipe.toEditUiState(): RecipeEditUiState {
    val servingsString = this.servings
    val servingsExtracted = Regex("\\d+").find(servingsString)?.value ?: ""

    val timeString = this.time
    val timeExtracted = Regex("\\d+").find(timeString)?.value ?: ""

    return RecipeEditUiState(
        title = this.title,
        servingsState = servingsExtracted,
        timeState = timeExtracted,
        level = this.level,
        categoryState = this.searchMetadata?.categoryFilter ?: "상관없음",
        utensilState = this.searchMetadata?.utensilFilter ?: "상관없음",
        ingredientsState = this.ingredients.map {
            IngredientUiState(
                name = it.name,
                quantity = it.quantity,
                isEssential = it.isEssential
            )
        },
        stepsState = this.steps.map { StepUiState(it.number, it.description) },
        imageUri = this.imageUri
    )
}