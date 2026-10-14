package com.kevin.memoverse.presentation.profile

data class ProfileUiState(
    val isConfirmingReset: Boolean = false,
    val isResetDone: Boolean = false,
    val hasResetFailed: Boolean = false,
)
