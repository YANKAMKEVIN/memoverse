package com.kevin.memoverse.presentation.themedetail

sealed interface ThemeDetailEvent {
    data class VerseToggled(val verseId: String) : ThemeDetailEvent
    data object StartClicked : ThemeDetailEvent
    data object RetryClicked : ThemeDetailEvent
    /** La scène de plantation s'est terminée ou a été passée d'un tap. */
    data object CeremonyFinished : ThemeDetailEvent
}
