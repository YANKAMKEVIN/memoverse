package com.kevin.memoverse.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.usecase.ResetGardenUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val resetGarden: ResetGardenUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.ResetClicked -> _state.update { ProfileUiState(isConfirmingReset = true) }
            ProfileEvent.ResetCancelled -> _state.update { it.copy(isConfirmingReset = false) }
            ProfileEvent.ResetConfirmed -> {
                _state.update { it.copy(isConfirmingReset = false) }
                viewModelScope.launch {
                    val ok = resetGarden() is DataResult.Success
                    _state.update { it.copy(isResetDone = ok, hasResetFailed = !ok) }
                }
            }
        }
    }
}
