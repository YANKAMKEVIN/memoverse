package com.kevin.memoverse.presentation.progression

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.memoverse.domain.usecase.ObserveGardenUseCase
import com.kevin.memoverse.domain.usecase.ObserveWateringStatsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ProgressionViewModel(
    observeGarden: ObserveGardenUseCase,
    observeWateringStats: ObserveWateringStatsUseCase,
) : ViewModel() {

    // Le jardin (références, textes, échéances) plutôt que les seuls stades : l'écran liste les versets.
    val state: StateFlow<ProgressionUiState> = combine(observeGarden(), observeWateringStats()) { garden, stats ->
        garden.toProgressionUiState().copy(totalXp = stats.totalXp, streakDays = stats.streakDays)
    }
        .catch { emit(ProgressionUiState(isLoading = false)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ProgressionUiState())

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
