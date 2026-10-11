package com.kevin.memoverse.presentation.themedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.LearningVerse
import com.kevin.memoverse.domain.model.StartLearningResult
import com.kevin.memoverse.domain.model.VerseSelection
import com.kevin.memoverse.domain.usecase.GetThemeWithVersesUseCase
import com.kevin.memoverse.domain.usecase.ObserveLearningVersesUseCase
import com.kevin.memoverse.domain.usecase.StartLearningUseCase
import com.kevin.memoverse.domain.usecase.ToggleVerseSelectionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ThemeDetailViewModel(
    private val themeId: String,
    private val getThemeWithVerses: GetThemeWithVersesUseCase,
    private val toggleVerseSelection: ToggleVerseSelectionUseCase,
    observeLearningVerses: ObserveLearningVersesUseCase,
    private val startLearning: StartLearningUseCase,
) : ViewModel() {

    private val themeLoad = MutableStateFlow<ThemeLoad>(ThemeLoad.Loading)
    private val selection = MutableStateFlow(VerseSelection())
    private val startStatus = MutableStateFlow<StartStatus>(StartStatus.Idle)
    private val learning: StateFlow<List<LearningVerse>> = observeLearningVerses()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val state: StateFlow<ThemeDetailUiState> =
        combine(themeLoad, selection, learning, startStatus, ::deriveThemeDetailUiState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ThemeDetailUiState())

    init {
        loadTheme()
    }

    fun onEvent(event: ThemeDetailEvent) {
        when (event) {
            is ThemeDetailEvent.VerseToggled -> toggle(event.verseId)
            ThemeDetailEvent.StartClicked -> start()
            ThemeDetailEvent.RetryClicked -> loadTheme()
        }
    }

    private fun toggle(verseId: String) {
        // Un verset déjà en apprentissage ne peut pas être choisi une seconde fois.
        if (learning.value.any { it.verseId == verseId }) return
        selection.update { toggleVerseSelection(it, verseId) }
        startStatus.update { if (it is StartStatus.Error) StartStatus.Idle else it }
    }

    private fun start() {
        if (startStatus.value == StartStatus.InProgress) return
        startStatus.value = StartStatus.InProgress
        viewModelScope.launch {
            startStatus.value = when (val result = startLearning(themeId, selection.value.selectedIds)) {
                is StartLearningResult.Started -> StartStatus.Started
                is StartLearningResult.TooManyActiveVerses ->
                    StartStatus.Error(StartErrorUi.TooManyActiveVerses(result.activeCount, result.maxActive))
                StartLearningResult.InvalidSelection, is StartLearningResult.Failed ->
                    StartStatus.Error(StartErrorUi.SaveFailed)
            }
        }
    }

    private fun loadTheme() {
        themeLoad.value = ThemeLoad.Loading
        viewModelScope.launch {
            themeLoad.value = when (val result = getThemeWithVerses(themeId)) {
                is DataResult.Success -> ThemeLoad.Loaded(result.data)
                is DataResult.Error -> ThemeLoad.Failed
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
