package com.kevin.memoverse.presentation.session

sealed interface SessionEvent {
    data class OptionSelected(val option: String) : SessionEvent
    data class ChunkPlaced(val chunkIndex: Int) : SessionEvent
    data class ChunkRemoved(val chunkIndex: Int) : SessionEvent
    data class WordChoiceTapped(val choiceIndex: Int) : SessionEvent
    data class BlankTapped(val slot: Int) : SessionEvent
    data class TextChanged(val text: String) : SessionEvent
    data class BlankTextChanged(val slot: Int, val text: String) : SessionEvent
    data object MicClicked : SessionEvent
    data object MicPermissionDenied : SessionEvent
    data object TypeInsteadClicked : SessionEvent
    /** Revenir à l'oral après « Écrire plutôt ». */
    data object SpeakInsteadClicked : SessionEvent
    data object CheckClicked : SessionEvent
    data object ContinueClicked : SessionEvent
    data object RetryClicked : SessionEvent
    data object CloseClicked : SessionEvent
    data object QuitConfirmed : SessionEvent
    data object QuitCancelled : SessionEvent
}
