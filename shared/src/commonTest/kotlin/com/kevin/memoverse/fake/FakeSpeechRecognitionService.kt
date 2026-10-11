package com.kevin.memoverse.fake

import com.kevin.memoverse.domain.speech.SpeechEvent
import com.kevin.memoverse.domain.speech.SpeechRecognitionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Rejoue des événements vocaux prédéfinis. */
class FakeSpeechRecognitionService(
    var events: List<SpeechEvent> = emptyList(),
    override val isAvailable: Boolean = true,
) : SpeechRecognitionService {
    var stopCount = 0

    override fun listen(languageTag: String): Flow<SpeechEvent> = flowOf(*events.toTypedArray())

    override fun stop() {
        stopCount++
    }
}
