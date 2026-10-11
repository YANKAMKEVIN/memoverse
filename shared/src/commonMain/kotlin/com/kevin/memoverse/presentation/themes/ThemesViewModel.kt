package com.kevin.memoverse.presentation.themes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.usecase.GetThemeFamiliesUseCase
import com.kevin.memoverse.domain.usecase.ObserveLearningVersesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemesViewModel(
    private val getThemeFamilies: GetThemeFamiliesUseCase,
    observeLearningVerses: ObserveLearningVersesUseCase,
) : ViewModel() {

    private val load = MutableStateFlow<ThemesLoad>(ThemesLoad.Loading)
    private val learningVerseIds = observeLearningVerses()
        .map { verses -> verses.map { it.verseId }.toSet() }
        .catch { emit(emptySet()) }

    val state: StateFlow<ThemesUiState> = combine(load, learningVerseIds, ::deriveThemesUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ThemesUiState())

    init {
        loadThemes()
    }

    fun onEvent(event: ThemesEvent) {
        when (event) {
            ThemesEvent.RetryClicked -> loadThemes()
        }
    }

    private fun loadThemes() {
        load.value = ThemesLoad.Loading
        viewModelScope.launch {
            load.value = when (val result = getThemeFamilies()) {
                is DataResult.Success -> ThemesLoad.Loaded(result.data)
                is DataResult.Error -> ThemesLoad.Failed
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
