package com.kjw.fridgerecipe.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.data.repository.TicketRepository
import com.kjw.fridgerecipe.domain.model.GeminiException
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.Recipe
import com.kjw.fridgerecipe.domain.repository.SettingsRepository
import com.kjw.fridgerecipe.domain.usecase.CheckIngredientConflictsUseCase
import com.kjw.fridgerecipe.domain.usecase.GetRecommendedRecipeUseCase
import com.kjw.fridgerecipe.domain.usecase.GetSavedRecipesUseCase
import com.kjw.fridgerecipe.presentation.util.UiText
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
    val title: UiText,
    val message: UiText
)

data class FilterOption<T>(
    val value: T,
    val label: UiText
)

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val getRecommendedRecipeUseCase: GetRecommendedRecipeUseCase,
    getSavedRecipesUseCase: GetSavedRecipesUseCase,
    private val checkIngredientConflictsUseCase: CheckIngredientConflictsUseCase,
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
        val LEVEL_FILTER_OPTIONS = listOf(
            FilterOption(null, UiText.StringResource(R.string.filter_any)),
            FilterOption(LevelType.BEGINNER, UiText.StringResource(R.string.level_beginner)),
            FilterOption(LevelType.INTERMEDIATE, UiText.StringResource(R.string.level_intermediate)),
            FilterOption(LevelType.ADVANCED, UiText.StringResource(R.string.level_advanced))
        )
        val CATEGORY_FILTER_OPTIONS = listOf(
            FilterOption(FILTER_ANY, UiText.StringResource(R.string.filter_any)),
            FilterOption("한식", UiText.StringResource(R.string.category_korean)),
            FilterOption("일식", UiText.StringResource(R.string.category_japanese)),
            FilterOption("중식", UiText.StringResource(R.string.category_chinese)),
            FilterOption("양식", UiText.StringResource(R.string.category_western))
        )
        val UTENSIL_FILTER_OPTIONS = listOf(
            FilterOption(FILTER_ANY, UiText.StringResource(R.string.filter_any)),
            FilterOption("에어프라이어", UiText.StringResource(R.string.utensil_airfryer)),
            FilterOption("전자레인지", UiText.StringResource(R.string.utensil_microwave)),
            FilterOption("냄비", UiText.StringResource(R.string.utensil_pot)),
            FilterOption("후라이팬", UiText.StringResource(R.string.utensil_pan))
        )
    }

    sealed interface HomeSideEffect {
        data class NavigateToRecipeDetail(val recipeId: Long) : HomeSideEffect
        data class ShowSnackbar(val message: UiText) : HomeSideEffect
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
    fun checkTicketReset() {
        viewModelScope.launch {
            ticketRepository.checkAndResetTicket()
        }
    }

    fun checkIngredientConflicts(selectedIngredients: List<Ingredient>) {
        viewModelScope.launch {
            val conflicts = checkIngredientConflictsUseCase(selectedIngredients)

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
                            _sideEffect.emit(
                                HomeSideEffect.ShowSnackbar(
                                    UiText.StringResource(R.string.msg_recipe_found_saved)
                                )
                            )
                            Log.d("ViewModel", "기존 레시피 제공으로 티켓 차감 안 함")
                        }
                    }
                } else {
                    if (isTicketUsed) ticketRepository.addTicket(1)
                    _homeUiState.update {
                        it.copy(
                            isRecipeLoading = false,
                            errorDialogState = ErrorDialogState(
                                title = UiText.StringResource(R.string.error_title_generic),
                                message = UiText.StringResource(R.string.error_msg_recipe_fetch_failed)
                            )
                        )
                    }
                }

            } catch (e: GeminiException) {
                ticketRepository.addTicket(1)
                val errorState = when (e) {
                    is GeminiException.QuotaExceeded -> ErrorDialogState(
                        title = UiText.StringResource(R.string.error_title_quota),
                        message = UiText.StringResource(R.string.error_msg_quota)
                    )
                    is GeminiException.ServerOverloaded -> ErrorDialogState(
                        title = UiText.StringResource(R.string.error_title_server),
                        message = UiText.StringResource(R.string.error_msg_server)
                    )
                    is GeminiException.ApiKeyError -> ErrorDialogState(
                        title = UiText.StringResource(R.string.error_title_update),
                        message = UiText.StringResource(R.string.error_msg_update)
                    )
                    is GeminiException.ParsingError -> ErrorDialogState(
                        title = UiText.StringResource(R.string.error_title_parsing),
                        message = UiText.StringResource(R.string.error_msg_parsing)
                    )
                    else -> ErrorDialogState(
                        title = UiText.StringResource(R.string.error_title_generic),
                        message = UiText.StringResource(R.string.error_msg_generic)
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
                        errorDialogState = ErrorDialogState(
                            title = UiText.StringResource(R.string.error_title_temp),
                            message = UiText.StringResource(R.string.error_msg_temp)
                        )
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
