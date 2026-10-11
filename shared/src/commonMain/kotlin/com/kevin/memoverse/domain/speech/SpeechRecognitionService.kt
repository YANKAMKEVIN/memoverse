package com.kevin.memoverse.domain.speech

import kotlinx.coroutines.flow.Flow

sealed interface SpeechEvent {
    /** Le micro est ouvert. */
    data object Listening : SpeechEvent
    data class Partial(val text: String) : SpeechEvent
    data class Final(val text: String) : SpeechEvent
    data class Failed(val error: SpeechError) : SpeechEvent
}

enum class SpeechError {
    PERMISSION_DENIED,
    UNAVAILABLE,
    /** Le pack de reconnaissance française n'est pas installé sur l'appareil. */
    LANGUAGE_PACK_MISSING,
    NO_MATCH,
    OTHER,
}

/** Reconnaissance vocale de la plateforme, de préférence sur l'appareil (l'audio ne sort pas). */
interface SpeechRecognitionService {
    val isAvailable: Boolean

    /** Écoute jusqu'au résultat final ; annuler le Flow interrompt l'écoute. */
    fun listen(languageTag: String): Flow<SpeechEvent>

    /** Termine l'écoute en cours en gardant ce qui a été dit (émet un [SpeechEvent.Final]). */
    fun stop()
}
