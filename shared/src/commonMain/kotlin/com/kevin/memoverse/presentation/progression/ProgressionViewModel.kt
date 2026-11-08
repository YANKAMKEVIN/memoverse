package com.kevin.memoverse.presentation.progression

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.memoverse.domain.usecase.ObserveLearningVersesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ProgressionViewModel(
    private val observeLearningVerses: ObserveLearningVersesUseCase,
) : ViewModel() {

    private val retries = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ProgressionUiState> = retries
        .flatMapLatest {
            observeLearningVerses()
                .map { it.toProgressionUiState() }
                .catch { emit(ProgressionUiState(isLoading = false, hasError = true)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ProgressionUiState())

    fun retry() {
        retries.update { it + 1 }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
