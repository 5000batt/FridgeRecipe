package com.kjw.fridgerecipe.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.data.repository.TicketRepository
import com.kjw.fridgerecipe.domain.model.GeminiException
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.repository.SettingsRepository
import com.kjw.fridgerecipe.domain.usecase.CheckIngredientConflictsUseCase
import com.kjw.fridgerecipe.domain.usecase.GetIngredientsUseCase
import com.kjw.fridgerecipe.domain.usecase.GetRecommendedRecipeUseCase
import com.kjw.fridgerecipe.presentation.ui.model.ErrorDialogState
import com.kjw.fridgerecipe.presentation.ui.model.HomeUiState
import com.kjw.fridgerecipe.presentation.ui.model.RecipeFilterState
import com.kjw.fridgerecipe.presentation.util.RecipeConstants.FILTER_ANY
import com.kjw.fridgerecipe.presentation.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getIngredientsUseCase: GetIngredientsUseCase, // 재료 로드
    private val getRecommendedRecipeUseCase: GetRecommendedRecipeUseCase, // 레시피 추천
    private val checkIngredientConflictsUseCase: CheckIngredientConflictsUseCase, // 제외 재료 충돌 확인
    private val settingsRepository: SettingsRepository,
    private val ticketRepository: TicketRepository
) : ViewModel() {

    sealed interface HomeSideEffect {
        data class NavigateToRecipeDetail(val recipeId: Long) : HomeSideEffect
        data class ShowSnackbar(val message: UiText) : HomeSideEffect
    }

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<HomeSideEffect>()
    val sideEffect: SharedFlow<HomeSideEffect> = _sideEffect.asSharedFlow()

    // 초기 데이터 로딩 완료 여부 체크
    private val _isDataLoaded = MutableStateFlow(false)
    val isDataLoaded: StateFlow<Boolean> = _isDataLoaded.asStateFlow()

    private val _seenRecipeIds = MutableStateFlow<Set<Long>>(emptySet())
    private var currentIngredientsQuery: String = ""

    init {
        loadIngredients()
        observeTickets()
        checkTicketReset()
    }

    // 재료 로드
    private fun loadIngredients() {
        viewModelScope.launch {
            getIngredientsUseCase().collect { ingredients ->
                val grouped = ingredients.groupBy { it.storageLocation }
                _homeUiState.update {
                    it.copy(
                        allIngredients = ingredients,
                        storageIngredients = grouped
                    )
                }
                _isDataLoaded.value = true
            }
        }
    }

    // 티켓 갯수 확인
    private fun observeTickets() {
        viewModelScope.launch {
            ticketRepository.ticketCount.collect { count ->
                _homeUiState.update { it.copy(remainingTickets = count) }
            }
        }
    }

    // 티켓 갯수 리셋
    fun checkTicketReset() {
        viewModelScope.launch {
            ticketRepository.checkAndResetTicket()
        }
    }

    // 재료 선택
    fun toggleIngredientSelection(id: Long) {
        _homeUiState.update { currentState ->
            val currentIds = currentState.selectedIngredientIds
            val newIds = if (id in currentIds) currentIds - id else currentIds + id
            currentState.copy(selectedIngredientIds = newIds)
        }
    }

    // 제외 재료 충돌 확인
    fun checkIngredientConflicts() {
        val selectedIds = _homeUiState.value.selectedIngredientIds
        val allIngredients = _homeUiState.value.allIngredients
        val selectedIngredients = allIngredients.filter { it.id in selectedIds }

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

    // 제외 재료 허용
    fun onConfirmConflict() {
        val selectedIds = _homeUiState.value.selectedIngredientIds
        val allIngredients = _homeUiState.value.allIngredients
        val selectedIngredients = allIngredients.filter { it.id in selectedIds }
        fetchRecommendedRecipe(selectedIngredients)
    }

    // 레시피 추천 요청
    private fun fetchRecommendedRecipe(selectedIngredients: List<Ingredient>) {
        _homeUiState.update { it.copy(showConflictDialog = false, conflictIngredients = emptyList()) }

        viewModelScope.launch {
            var isTicketUsed = false

            _homeUiState.update { it.copy(isRecipeLoading = true) }
            try {
                // 선택한 재료 이름만 추출
                val ingredientsQuery = selectedIngredients
                    .map { it.name }
                    .sorted()
                    .joinToString(",")

                // 추천 중복 방지
                if (ingredientsQuery != currentIngredientsQuery) {
                    _seenRecipeIds.value = emptySet()
                    currentIngredientsQuery = ingredientsQuery
                }

                // 제외 재료
                val excludedList = settingsRepository.excludedIngredients.first().toList()
                val selectedIngredientNames = selectedIngredients.map { it.name }
                val finalExcludedList = excludedList.filter { it !in selectedIngredientNames }

                // 검색 필터
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
                        if (_homeUiState.value.remainingTickets <= 0) {
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

    // Filter & Dialog Handlers

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
}