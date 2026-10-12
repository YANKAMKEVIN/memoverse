package com.kevin.memoverse.presentation.profile

import com.kevin.memoverse.presentation.common.TypefaceUi

data class ProfileUiState(
    val isConfirmingReset: Boolean = false,
    val isResetDone: Boolean = false,
    val hasResetFailed: Boolean = false,
    val typefaces: List<TypefaceOptionUi> = emptyList(),
    val hasTypefaceSaveFailed: Boolean = false,
)

data class TypefaceOptionUi(val typeface: TypefaceUi, val isSelected: Boolean)
