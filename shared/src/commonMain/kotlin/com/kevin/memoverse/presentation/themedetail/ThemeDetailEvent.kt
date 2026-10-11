package com.kevin.memoverse.presentation.themedetail

sealed interface ThemeDetailEvent {
    data class VerseToggled(val verseId: String) : ThemeDetailEvent
    data object StartClicked : ThemeDetailEvent
    data object RetryClicked : ThemeDetailEvent
}
