package com.kevin.memoverse.presentation.garden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.memoverse.domain.usecase.ObserveGardenUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class GardenViewModel(
    private val observeGarden: ObserveGardenUseCase,
) : ViewModel() {

    private val retries = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<GardenUiState> = retries
        .flatMapLatest {
            observeGarden()
                .map { it.toGardenUiState() }
                .catch { emit(GardenUiState(isLoading = false, hasError = true)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), GardenUiState())

    fun retry() {
        retries.update { it + 1 }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
