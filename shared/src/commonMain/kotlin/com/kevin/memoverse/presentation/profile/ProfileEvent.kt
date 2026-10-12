package com.kevin.memoverse.presentation.profile

import com.kevin.memoverse.presentation.common.TypefaceUi

sealed interface ProfileEvent {
    data object ResetClicked : ProfileEvent
    data object ResetConfirmed : ProfileEvent
    data object ResetCancelled : ProfileEvent
    data class TypefaceSelected(val typeface: TypefaceUi) : ProfileEvent
}
