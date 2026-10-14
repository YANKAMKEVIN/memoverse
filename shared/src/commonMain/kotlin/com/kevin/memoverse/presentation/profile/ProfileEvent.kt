package com.kevin.memoverse.presentation.profile

sealed interface ProfileEvent {
    data object ResetClicked : ProfileEvent
    data object ResetConfirmed : ProfileEvent
    data object ResetCancelled : ProfileEvent
}
