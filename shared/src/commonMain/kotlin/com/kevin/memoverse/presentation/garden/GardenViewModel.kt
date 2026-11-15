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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class GardenViewModel(
    observeGarden: ObserveGardenUseCase,
) : ViewModel() {

    private val attempts = MutableStateFlow(0)

    // Une erreur de lecture n'est plus montrée comme un jardin vide (on croyait avoir perdu ses versets) :
    // l'écran affiche une erreur et « Réessayer » relance l'observation.
    val state: StateFlow<GardenUiState> = attempts
        .flatMapLatest {
            observeGarden()
                .map { it.toGardenUiState() }
                .onStart { emit(GardenUiState()) }
                .catch { emit(GardenUiState(isLoading = false, hasError = true)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), GardenUiState())

    fun onRetry() {
        attempts.update { it + 1 }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
