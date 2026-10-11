package com.kevin.memoverse.presentation.themedetail

import com.kevin.memoverse.domain.model.ThemeWithVerses

/** Sources d'état internes au ViewModel, combinées par [deriveThemeDetailUiState]. */
internal sealed interface ThemeLoad {
    data object Loading : ThemeLoad
    data class Loaded(val themeWithVerses: ThemeWithVerses) : ThemeLoad
    data object Failed : ThemeLoad
}

internal sealed interface StartStatus {
    data object Idle : StartStatus
    data object InProgress : StartStatus
    data object Started : StartStatus
    data class Error(val error: StartErrorUi) : StartStatus
}
