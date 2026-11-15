package com.kevin.memoverse.fake

import com.kevin.memoverse.domain.speech.SpeechEvent
import com.kevin.memoverse.domain.speech.SpeechRecognitionService
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Rejoue des événements vocaux prédéfinis. */
class FakeSpeechRecognitionService(
    var events: List<SpeechEvent> = emptyList(),
    override val isAvailable: Boolean = true,
) : SpeechRecognitionService {
    var stopCount = 0

    /** Si vrai, le flux reste ouvert après les événements (micro toujours actif), jusqu'à annulation. */
    var keepListening = false

    override fun listen(languageTag: String): Flow<SpeechEvent> = flow {
        events.forEach { emit(it) }
        if (keepListening) awaitCancellation()
    }

    override fun stop() {
        stopCount++
    }
}
