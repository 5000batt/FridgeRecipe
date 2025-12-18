package com.kjw.fridgerecipe.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.data.repository.TicketRepository
import com.kjw.fridgerecipe.domain.model.GeminiException
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.repository.SettingsRepository
import com.kjw.fridgerecipe.domain.usecase.GetRecommendedRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val FILTER_ANY = "상관없음"
data class RecipeFilterState(
    val timeLimit: String? = null,
    val level: LevelType? = null,
    val category: String? = null,
    val utensil: String? = null,
    val useOnlySelected: Boolean = false
)

data class HomeUiState(
    val recommendedRecipe: Recipe? = null,
    val isRecipeLoading: Boolean = false,
    val selectedIngredientIds: Set<Long> = emptySet(),
    val filterState: RecipeFilterState = RecipeFilterState(),
    val showConflictDialog: Boolean = false,
    val conflictIngredients: List<String> = emptyList(),
    val errorDialogState: ErrorDialogState? = null,
    val showAdDialog: Boolean = false
)

data class ErrorDialogState(
    val title: String,
    val message: String
)

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val getRecommendedRecipeUseCase: GetRecommendedRecipeUseCase,
    getSavedRecipesUseCase: GetSavedRecipesUseCase,
    private val settingsRepository: SettingsRepository,
    private val ticketRepository: TicketRepository
) : ViewModel() {

    val remainingTickets: StateFlow<Int> = ticketRepository.ticketCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

    init {
        viewModelScope.launch {
            ticketRepository.checkAndResetTicket()
        }
    }

    companion object {
        val LEVEL_FILTER_OPTIONS = listOf(null, LevelType.BEGINNER, LevelType.INTERMEDIATE, LevelType.ADVANCED)
        val CATEGORY_FILTER_OPTIONS = listOf(FILTER_ANY, "한식", "일식", "중식", "양식")
        val UTENSIL_FILTER_OPTIONS = listOf(FILTER_ANY, "에어프라이어", "전자레인지", "냄비", "후라이팬")
    }

    sealed interface HomeSideEffect {
        data class NavigateToRecipeDetail(val recipeId: Long) : HomeSideEffect
        data class ShowSnackbar(val message: String) : HomeSideEffect
    }

    private val _sideEffect = MutableSharedFlow<HomeSideEffect>()
    val sideEffect: SharedFlow<HomeSideEffect> = _sideEffect.asSharedFlow()

    // HomeScreen States
    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()
    private val _seenRecipeIds = MutableStateFlow<Set<Long>>(emptySet())

    // RecipeListScreen States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _rawSavedRecipesFlow = getSavedRecipesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val rawSavedRecipes: StateFlow<List<Recipe>> = _rawSavedRecipesFlow

    val savedRecipes: StateFlow<List<Recipe>> =
        _searchQuery.combine(_rawSavedRecipesFlow) { query, recipes ->
            if (query.isBlank()) {
                recipes
            } else {
                recipes.filter {
                    it.title.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var currentIngredientsQuery: String = ""

    // HomeScreen Functions
    fun checkIngredientConflicts(selectedIngredients: List<Ingredient>) {
        viewModelScope.launch {
            val excludedList = settingsRepository.excludedIngredients.first().toSet()

            val conflicts = selectedIngredients
                .map { it.name }
                .filter { it in excludedList }

            if (conflicts.isNotEmpty()) {
                _homeUiState.update {
                    it.copy(
                        showConflictDialog = true,
                        conflictIngredients = conflicts
                    )
                }
            } else {
                fetchRecommendedRecipe(selectedIngredients)
            }
        }
    }

    fun fetchRecommendedRecipe(selectedIngredients: List<Ingredient>) {
        _homeUiState.update { it.copy(showConflictDialog = false, conflictIngredients = emptyList()) }

        viewModelScope.launch {
            var isTicketUsed = false

            _homeUiState.update { it.copy(isRecipeLoading = true) }
            try {
                val ingredientsQuery = selectedIngredients
                    .map { it.name }
                    .sorted()
                    .joinToString(",")

                if (ingredientsQuery != currentIngredientsQuery) {
                    _seenRecipeIds.value = emptySet()
                    currentIngredientsQuery = ingredientsQuery
                }

                val excludedList = settingsRepository.excludedIngredients.first().toList()
                val selectedIngredientNames = selectedIngredients.map { it.name }.toSet()
                val finalExcludedList = excludedList.filter { it !in selectedIngredientNames }

                val currentFilters = _homeUiState.value.filterState

                val newRecipe = getRecommendedRecipeUseCase(
                    ingredients = selectedIngredients,
                    seenIds = _seenRecipeIds.value,
                    timeFilter = currentFilters.timeLimit,
                    levelFilter = currentFilters.level,
                    categoryFilter = currentFilters.category,
                    utensilFilter = currentFilters.utensil,
                    useOnlySelected = currentFilters.useOnlySelected,
                    excludedIngredients = finalExcludedList,
                    onAiCall = {
                        if (remainingTickets.value <= 0) {
                            _homeUiState.update { it.copy(showAdDialog = true, isRecipeLoading = false) }
                            throw Exception("Ticket Exhausted")
                        }
                        ticketRepository.useTicket()
                        isTicketUsed = true
                    }
                )

                _homeUiState.update { it.copy(recommendedRecipe = newRecipe) }

                if (newRecipe != null) {
                    newRecipe.id?.let { recipeId ->
                        _seenRecipeIds.value = _seenRecipeIds.value + recipeId
                        _sideEffect.emit(HomeSideEffect.NavigateToRecipeDetail(recipeId))

                        if (!isTicketUsed) {
                            _sideEffect.emit(HomeSideEffect.ShowSnackbar("✨ 보관함에서 레시피를 찾았어요! 티켓이 차감되지 않았습니다."))
                            Log.d("ViewModel", "기존 레시피 제공으로 티켓 차감 안 함")
                        }
                    }
                } else {
                    if (isTicketUsed) ticketRepository.addTicket(1)
                    _homeUiState.update {
                        it.copy(
                            isRecipeLoading = false,
                            errorDialogState = ErrorDialogState(
                                title = "오류 발생",
                                message = "레시피 데이터를 불러오지 못했습니다.\n다시 시도해주세요."
                            )
                        )
                    }
                }

            } catch (e: GeminiException) {
                ticketRepository.addTicket(1)
                val errorState = when (e) {
                    is GeminiException.QuotaExceeded -> ErrorDialogState(
                        title = "주문 폭주! \uD83D\uDC68\u200D\uD83C\uDF73",
                        message = "현재 요청이 너무 많아 AI 셰프가 바쁩니다! \uD83D\uDC68\u200D\uD83C\uDF73\\n잠시 후 다시 시도하거나 내일 이용해 주세요."
                    )
                    is GeminiException.ServerOverloaded -> ErrorDialogState(
                        title = "서버 연결 지연",
                        message = "AI 서버가 잠시 응답하지 않습니다.\n잠시 후 다시 시도해주세요."
                    )
                    is GeminiException.ApiKeyError -> ErrorDialogState(
                        title = "업데이트 필요",
                        message = "서비스 연결 정보가 변경되었습니다. 최신 버전으로 업데이트 또는 문의해주세요."
                    )
                    is GeminiException.ParsingError -> ErrorDialogState(
                        title = "레시피 생성 실패",
                        message = "AI가 레시피를 만드는 데 실패했습니다.\n다시 시도해주세요."
                    )
                    else -> ErrorDialogState(
                        title = "오류 발생",
                        message = "알 수 없는 오류가 발생했습니다.\n다시 시도해주세요."
                    )
                }

                _homeUiState.update {
                    it.copy(isRecipeLoading = false, errorDialogState = errorState)
                }
            } catch (e: Exception) {
                if (e.message == "Ticket Exhausted") return@launch
                if (isTicketUsed) ticketRepository.addTicket(1)
                _homeUiState.update {
                    it.copy(
                        isRecipeLoading = false,
                        errorDialogState = ErrorDialogState("오류", "일시적인 오류입니다.")
                    )
                }
            }
        }
    }

    fun dismissAdDialog() {
        _homeUiState.update { it.copy(showAdDialog = false) }
    }

    fun onAdWatched() {
        viewModelScope.launch {
            ticketRepository.addTicket(1)
            dismissAdDialog()
        }
    }

    fun dismissErrorDialog() {
        _homeUiState.update { it.copy(errorDialogState = null) }
    }

    fun dismissConflictDialog() {
        _homeUiState.update { it.copy(showConflictDialog = false, conflictIngredients = emptyList()) }
    }

    fun toggleIngredientSelection(id: Long) {
        _homeUiState.update { currentState ->
            val currentIds = currentState.selectedIngredientIds
            val newIds = if (id in currentIds) currentIds - id else currentIds + id
            currentState.copy(selectedIngredientIds = newIds)
        }
    }

    fun onTimeFilterChanged(time: String) {
        _homeUiState.update { state ->
            state.copy(filterState = state.filterState.copy(
                timeLimit = if (time == FILTER_ANY) null else time
            ))
        }
    }

    fun onLevelFilterChanged(level: LevelType?) {
        _homeUiState.update { state ->
            state.copy(filterState = state.filterState.copy(level = level))
        }
    }

    fun onCategoryFilterChanged(category: String) {
        _homeUiState.update { state ->
            state.copy(filterState = state.filterState.copy(
                category = if (category == FILTER_ANY) null else category
            ))
        }
    }

    fun onUtensilFilterChanged(utensil: String) {
        _homeUiState.update { state ->
            state.copy(filterState = state.filterState.copy(
                utensil = if (utensil == FILTER_ANY) null else utensil
            ))
        }
    }

    fun onUseOnlySelectedIngredientsChanged(isChecked: Boolean) {
        _homeUiState.update { state ->
            state.copy(filterState = state.filterState.copy(useOnlySelected = isChecked))
        }
    }

    fun resetHomeState() {
        _homeUiState.update { state ->
            state.copy(
                isRecipeLoading = false,
                selectedIngredientIds = emptySet(),
                filterState = RecipeFilterState(),
                recommendedRecipe = null
            )
        }
    }

    // RecipeListScreen Function
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }
}
