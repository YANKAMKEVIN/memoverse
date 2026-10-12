package com.kevin.memoverse.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.TypefaceChoice
import com.kevin.memoverse.domain.usecase.ObserveTypefaceUseCase
import com.kevin.memoverse.domain.usecase.ResetGardenUseCase
import com.kevin.memoverse.domain.usecase.SelectTypefaceUseCase
import com.kevin.memoverse.presentation.common.toDomain
import com.kevin.memoverse.presentation.common.toUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val resetGarden: ResetGardenUseCase,
    observeTypeface: ObserveTypefaceUseCase,
    private val selectTypeface: SelectTypefaceUseCase,
) : ViewModel() {

    /** État propre à l'écran (confirmations, erreurs) ; la police vient de la base. */
    private val local = MutableStateFlow(ProfileUiState())

    val state: StateFlow<ProfileUiState> = combine(local, observeTypeface()) { screen, typeface -> screen.withTypeface(typeface) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ProfileUiState())

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.ResetClicked -> local.update { ProfileUiState(isConfirmingReset = true) }
            ProfileEvent.ResetCancelled -> local.update { it.copy(isConfirmingReset = false) }
            ProfileEvent.ResetConfirmed -> {
                local.update { it.copy(isConfirmingReset = false) }
                viewModelScope.launch {
                    val ok = resetGarden() is DataResult.Success
                    local.update { it.copy(isResetDone = ok, hasResetFailed = !ok) }
                }
            }
            is ProfileEvent.TypefaceSelected -> viewModelScope.launch {
                val ok = selectTypeface(event.typeface.toDomain()) is DataResult.Success
                local.update { it.copy(hasTypefaceSaveFailed = !ok) }
            }
        }
    }
}

internal fun ProfileUiState.withTypeface(selected: TypefaceChoice): ProfileUiState = copy(
    typefaces = TypefaceChoice.entries.map { TypefaceOptionUi(it.toUi(), isSelected = it == selected) },
)
