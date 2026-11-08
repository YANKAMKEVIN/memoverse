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

    /** Vrai : l'écoute reste ouverte après les événements, comme un vrai micro. */
    var keepsListening = false

    override fun listen(languageTag: String): Flow<SpeechEvent> = flow {
        events.forEach { emit(it) }
        if (keepsListening) awaitCancellation()
    }

    override fun stop() {
        stopCount++
    }
}
