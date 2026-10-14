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
    /** Versets enregistrés : la scène de plantation se joue. */
    data object Started : StartStatus
    /** Scène jouée ou passée : on peut rejoindre le jardin. */
    data object Planted : StartStatus
    data class Error(val error: StartErrorUi) : StartStatus
}
