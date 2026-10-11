package com.kevin.memoverse.presentation.themes

sealed interface ThemesEvent {
    data object RetryClicked : ThemesEvent
}
