package com.kjw.fridgerecipe.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kjw.fridgerecipe.R
import com.kjw.fridgerecipe.data.repository.TicketRepository
import com.kjw.fridgerecipe.domain.model.CookingToolType
import com.kjw.fridgerecipe.domain.model.Ingredient
import com.kjw.fridgerecipe.domain.model.LevelType
import com.kjw.fridgerecipe.domain.model.RecipeCategoryType
import com.kjw.fridgerecipe.domain.model.IngredientCategoryType
import com.kjw.fridgerecipe.domain.model.TicketException
import com.kjw.fridgerecipe.domain.repository.SettingsRepository
import com.kjw.fridgerecipe.domain.usecase.CheckIngredientConflictsUseCase
import com.kjw.fridgerecipe.domain.usecase.GetIngredientsUseCase
import com.kjw.fridgerecipe.domain.usecase.GetRecommendedRecipeUseCase
import com.kjw.fridgerecipe.domain.util.DataResult
import com.kjw.fridgerecipe.presentation.ui.model.ErrorDialogState
import com.kjw.fridgerecipe.presentation.ui.model.HomeUiState
import com.kjw.fridgerecipe.presentation.ui.model.RecipeFilterState
import com.kjw.fridgerecipe.presentation.util.RecipeConstants.FILTER_ANY
import com.kjw.fridgerecipe.presentation.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    private val getIngredientsUseCase: GetIngredientsUseCase,
    private val getRecommendedRecipeUseCase: GetRecommendedRecipeUseCase,
    private val checkIngredientConflictsUseCase: CheckIngredientConflictsUseCase,
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

    private val _seenRecipeIds = MutableStateFlow<Set<Long>>(emptySet())
    private var currentIngredientsQuery: String = ""

    private var currentCategoryFilter: IngredientCategoryType? = null

    init {
        loadIngredients()
        observeTickets()
        checkTicketReset()
        observeSettings()
    }

    private fun loadIngredients() {
        viewModelScope.launch {
            getIngredientsUseCase().collect { ingredients ->
                _homeUiState.update {
                    it.copy(
                        allIngredients = ingredients,
                        isIngredientLoading = false
                    )
                }
                updateDisplayedIngredients()
            }
        }
    }

    fun onCategorySelect(category: IngredientCategoryType?) {
        currentCategoryFilter = category
        _homeUiState.update { it.copy(selectedCategory = category) }
        updateDisplayedIngredients()
    }

    private fun updateDisplayedIngredients() {
        val all = _homeUiState.value.allIngredients
        val category = currentCategoryFilter

        val filtered = if (category == null) all else all.filter { it.category == category }
        val sorted = filtered.sortedBy { it.expirationDate }
        val grouped = sorted.groupBy { it.storageLocation }

        _homeUiState.update { it.copy(storageIngredients = grouped) }
    }

    private fun observeTickets() {
        viewModelScope.launch {
            ticketRepository.ticketCount.collect { count ->
                _homeUiState.update { it.copy(remainingTickets = count) }
            }
        }
    }

    fun checkTicketReset() {
        viewModelScope.launch {
            ticketRepository.checkAndResetTicket()
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.isIngredientCheckSkip.collect { isSkip ->
                _homeUiState.update { it.copy(isIngredientCheckSkip = isSkip) }
            }
        }

        viewModelScope.launch {
            settingsRepository.isFirstLaunch.collect { isFirst ->
                _homeUiState.update { it.copy(isFirstLaunch = isFirst) }
            }
        }
    }

    // 가이드 완료 시 호출
    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setFirstLaunchComplete()
        }
    }

    // 알림 권한 여부
    fun setNotificationEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationEnabled(isEnabled)
        }
    }

    fun toggleIngredientSelection(id: Long) {
        _homeUiState.update { currentState ->
            val currentIds = currentState.selectedIngredientIds
            val newIds = if (id in currentIds) currentIds - id else currentIds + id
            currentState.copy(selectedIngredientIds = newIds)
        }
    }

    fun onRecommendButtonClick() {
        val uiState = _homeUiState.value
        if (uiState.selectedIngredientIds.isEmpty()) return

        if (uiState.isIngredientCheckSkip) {
            checkIngredientConflicts()
        } else {
            _homeUiState.update { it.copy(showIngredientCheckDialog = true) }
        }
    }

    fun onConfirmIngredientCheck(doNotShowAgain: Boolean) {
        _homeUiState.update { it.copy(showIngredientCheckDialog = false) }
        if (doNotShowAgain) {
            viewModelScope.launch { settingsRepository.setIngredientCheckSkip(true) }
        }
        checkIngredientConflicts()
    }

    fun checkIngredientConflicts() {
        val selectedIds = _homeUiState.value.selectedIngredientIds
        val allIngredients = _homeUiState.value.allIngredients
        val selectedIngredients = allIngredients.filter { it.id in selectedIds }

        viewModelScope.launch {
            val conflicts = checkIngredientConflictsUseCase(selectedIngredients)
            if (conflicts.isNotEmpty()) {
                _homeUiState.update { it.copy(showConflictDialog = true, conflictIngredients = conflicts) }
            } else {
                fetchRecommendedRecipe(selectedIngredients)
            }
        }
    }

    fun onConfirmConflict() {
        val selectedIds = _homeUiState.value.selectedIngredientIds
        val allIngredients = _homeUiState.value.allIngredients
        val selectedIngredients = allIngredients.filter { it.id in selectedIds }
        fetchRecommendedRecipe(selectedIngredients)
    }

    fun onAdLoaded() {
        _homeUiState.update { it.copy(isAdLoaded = true) }
    }

    fun testAddTicket() {
        viewModelScope.launch {
            ticketRepository.addTicket(3)
        }
    }

    fun testUseTicket() {
        viewModelScope.launch {
            ticketRepository.useTicket()
        }
    }

    private fun fetchRecommendedRecipe(selectedIngredients: List<Ingredient>) {
        _homeUiState.update { it.copy(showConflictDialog = false, conflictIngredients = emptyList()) }

        viewModelScope.launch {
            var isTicketUsed = false
            val startTime = System.currentTimeMillis()
            val minDataLoadingTime = 3000L // 최소 데이터 로딩 시간
            val minAdDisplayTime = 2500L  // 광고 로드 후 최소 노출 시간

            _homeUiState.update { it.copy(isRecipeLoading = true, isAdLoaded = false) }

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
                val selectedIngredientNames = selectedIngredients.map { it.name }
                val finalExcludedList = excludedList.filter { it !in selectedIngredientNames }

                val currentFilters = _homeUiState.value.filterState

                // 레시피 데이터 호출
                val result = getRecommendedRecipeUseCase(
                    ingredients = selectedIngredients,
                    seenIds = _seenRecipeIds.value,
                    timeFilter = currentFilters.timeLimit,
                    level = currentFilters.level,
                    categoryFilter = currentFilters.category,
                    cookingToolFilter = currentFilters.cookingTool,
                    useOnlySelected = currentFilters.useOnlySelected,
                    excludedIngredients = finalExcludedList,
                    onAiCall = {
                        if (_homeUiState.value.remainingTickets <= 0) {
                            _homeUiState.update { it.copy(showAdDialog = true, isRecipeLoading = false) }
                            throw TicketException.Exhausted
                        }
                        ticketRepository.useTicket()
                        isTicketUsed = true
                    }
                )

                // 최소 레시피 데이터 로딩 시간 대기
                val dataElapsedTime = System.currentTimeMillis() - startTime
                if (dataElapsedTime < minDataLoadingTime) delay(minDataLoadingTime - dataElapsedTime)

                // 광고가 로드될 때까지 최대 3초 더 대기
                var adWaitTime = 0L
                val maxAdWaitTime = 3000L
                while (!_homeUiState.value.isAdLoaded && adWaitTime < maxAdWaitTime) {
                    delay(200)
                    adWaitTime += 200
                }

                // 광고 로드 성공했다면 최소 노출 시간(2.5초) 보장
                if (_homeUiState.value.isAdLoaded) {
                    delay(minAdDisplayTime)
                }

                when (result) {
                    is DataResult.Success -> {
                        val newRecipe = result.data
                        _homeUiState.update { it.copy(recommendedRecipe = newRecipe, isRecipeLoading = false) }

                        newRecipe.id?.let { recipeId ->
                            _seenRecipeIds.value = _seenRecipeIds.value + recipeId
                            _sideEffect.emit(HomeSideEffect.NavigateToRecipeDetail(recipeId))

                            if (!isTicketUsed) {
                                _sideEffect.emit(HomeSideEffect.ShowSnackbar(UiText.StringResource(R.string.msg_recipe_found_saved)))
                            }
                        }
                    }
                    is DataResult.Error -> {
                        if (isTicketUsed) ticketRepository.addTicket(1)
                        _homeUiState.update {
                            it.copy(
                                isRecipeLoading = false,
                                errorDialogState = ErrorDialogState(
                                    title = result.title ?: UiText.StringResource(R.string.error_title_generic),
                                    message = result.message
                                )
                            )
                        }
                    }
                    else -> _homeUiState.update { it.copy(isRecipeLoading = false) }
                }

            } catch (e: TicketException.Exhausted) {
                // 티켓 소진 시 조용히 중단
            } catch (e: Exception) {
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

    fun onCategoryFilterChanged(category: RecipeCategoryType?) {
        _homeUiState.update { state ->
            state.copy(filterState = state.filterState.copy(category = category))
        }
    }

    fun onCookingToolFilterChanged(cookingTool: CookingToolType?) {
        _homeUiState.update { state ->
            state.copy(filterState = state.filterState.copy(cookingTool = cookingTool))
        }
    }

    fun onUseOnlySelectedIngredientsChanged(isChecked: Boolean) {
        _homeUiState.update { state ->
            state.copy(filterState = state.filterState.copy(useOnlySelected = isChecked))
        }
    }

    fun showAdDialog() { _homeUiState.update { it.copy(showAdDialog = true) } }
    fun dismissAdDialog() { _homeUiState.update { it.copy(showAdDialog = false) } }

    fun onAdWatched() {
        viewModelScope.launch {
            ticketRepository.addTicket(1)
            dismissAdDialog()
        }
    }

    fun dismissErrorDialog() { _homeUiState.update { it.copy(errorDialogState = null) } }
    fun dismissIngredientCheckDialog() { _homeUiState.update { it.copy(showIngredientCheckDialog = false) } }
    fun dismissConflictDialog() { _homeUiState.update { it.copy(showConflictDialog = false, conflictIngredients = emptyList()) } }

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

    fun clearRecommendedRecipe() {
        _homeUiState.update { state ->
            state.copy(isRecipeLoading = false, recommendedRecipe = null)
        }
    }
}
