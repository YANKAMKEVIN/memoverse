package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.speech.SpeechEvent
import com.kevin.memoverse.domain.speech.SpeechRecognitionService
import kotlinx.coroutines.flow.Flow

/** Écoute la récitation d'un verset (en français). */
class ListenToRecitationUseCase(
    private val service: SpeechRecognitionService,
) {
    operator fun invoke(): Flow<SpeechEvent> = service.listen(LANGUAGE_TAG)

    fun stop() = service.stop()

    private companion object {
        const val LANGUAGE_TAG = "fr-FR"
    }
}
