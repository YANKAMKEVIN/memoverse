package com.kevin.memoverse.presentation.progression

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.memoverse.domain.usecase.ObserveLearningVersesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProgressionViewModel(
    observeLearningVerses: ObserveLearningVersesUseCase,
) : ViewModel() {

    val state: StateFlow<ProgressionUiState> = observeLearningVerses()
        .map { it.toProgressionUiState() }
        .catch { emit(ProgressionUiState(isLoading = false)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ProgressionUiState())

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
