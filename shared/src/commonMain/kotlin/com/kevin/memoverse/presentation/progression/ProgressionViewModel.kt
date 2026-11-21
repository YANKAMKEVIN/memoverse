package com.kevin.memoverse.presentation.progression

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.memoverse.domain.usecase.ObserveGardenUseCase
import com.kevin.memoverse.domain.usecase.ObserveWateringStatsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressionViewModel(
    observeGarden: ObserveGardenUseCase,
    observeWateringStats: ObserveWateringStatsUseCase,
) : ViewModel() {

    private val attempts = MutableStateFlow(0)

    // Le jardin (références, textes, échéances) plutôt que les seuls stades : l'écran liste les versets.
    // Une erreur de lecture n'est pas montrée comme un jardin vide : erreur, et « Réessayer » relance l'observation.
    val state: StateFlow<ProgressionUiState> = attempts
        .flatMapLatest {
            combine(observeGarden(), observeWateringStats()) { garden, stats ->
                garden.toProgressionUiState().copy(totalXp = stats.totalXp, streakDays = stats.streakDays)
            }
                .onStart { emit(ProgressionUiState()) }
                .catch { emit(ProgressionUiState(isLoading = false, hasError = true)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ProgressionUiState())

    fun onRetry() {
        attempts.update { it + 1 }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
