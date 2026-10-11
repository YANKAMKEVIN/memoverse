package com.kevin.memoverse.presentation.themes

import com.kevin.memoverse.domain.model.ThemeFamily

internal sealed interface ThemesLoad {
    data object Loading : ThemesLoad
    data class Loaded(val families: List<ThemeFamily>) : ThemesLoad
    data object Failed : ThemesLoad
}
