package com.kevin.memoverse.presentation.garden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.memoverse.domain.usecase.ObserveGardenUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class GardenViewModel(
    observeGarden: ObserveGardenUseCase,
) : ViewModel() {

    val state: StateFlow<GardenUiState> = observeGarden()
        .map { it.toGardenUiState() }
        .catch { emit(GardenUiState(isLoading = false)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), GardenUiState())

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
