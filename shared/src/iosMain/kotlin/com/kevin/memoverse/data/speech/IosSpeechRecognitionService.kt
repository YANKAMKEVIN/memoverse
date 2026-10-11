package com.kevin.memoverse.data.speech

import com.kevin.memoverse.domain.speech.SpeechError
import com.kevin.memoverse.domain.speech.SpeechEvent
import com.kevin.memoverse.domain.speech.SpeechRecognitionService
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDuckOthers
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
import platform.AVFAudio.setActive
import platform.Foundation.NSLocale
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus

/** Reconnaissance vocale iOS (SFSpeechRecognizer), sur l'appareil quand le modèle français y est. */
@OptIn(ExperimentalForeignApi::class)
class IosSpeechRecognitionService : SpeechRecognitionService {

    private var request: SFSpeechAudioBufferRecognitionRequest? = null

    override val isAvailable: Boolean
        get() = recognizer("fr-FR")?.isAvailable() == true

    override fun listen(languageTag: String): Flow<SpeechEvent> = callbackFlow {
        val recognizer = recognizer(languageTag)
        if (recognizer == null || !recognizer.isAvailable()) {
            trySend(SpeechEvent.Failed(SpeechError.UNAVAILABLE))
            close()
            awaitClose()
            return@callbackFlow
        }
        if (SFSpeechRecognizer.authorizationStatus() != SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized) {
            trySend(SpeechEvent.Failed(SpeechError.PERMISSION_DENIED))
            close()
            awaitClose()
            return@callbackFlow
        }
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryRecord, AVAudioSessionModeMeasurement, AVAudioSessionCategoryOptionDuckOthers, null)
        session.setActive(true, AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation, null)

        val engine = AVAudioEngine()
        val bufferRequest = SFSpeechAudioBufferRecognitionRequest().apply {
            shouldReportPartialResults = true
            if (recognizer.supportsOnDeviceRecognition) requiresOnDeviceRecognition = true
        }
        request = bufferRequest
        val input = engine.inputNode
        input.installTapOnBus(0u, BUFFER_SIZE, input.outputFormatForBus(0u)) { buffer, _ ->
            buffer?.let { bufferRequest.appendAudioPCMBuffer(it) }
        }
        engine.prepare()
        if (!engine.startAndReturnError(null)) {
            trySend(SpeechEvent.Failed(SpeechError.OTHER))
            close()
        } else {
            trySend(SpeechEvent.Listening)
        }

        var task: SFSpeechRecognitionTask? = null
        task = recognizer.recognitionTaskWithRequest(bufferRequest) { result, error ->
            val text = result?.bestTranscription?.formattedString
            when {
                result != null && result.isFinal() -> {
                    trySend(if (text.isNullOrBlank()) SpeechEvent.Failed(SpeechError.NO_MATCH) else SpeechEvent.Final(text))
                    close()
                }
                text != null -> trySend(SpeechEvent.Partial(text))
                error != null -> {
                    trySend(SpeechEvent.Failed(SpeechError.NO_MATCH))
                    close()
                }
            }
        }
        awaitClose {
            engine.stop()
            input.removeTapOnBus(0u)
            task?.cancel()
            request = null
            session.setActive(false, AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation, null)
        }
    }

    override fun stop() {
        request?.endAudio()
    }

    private fun recognizer(languageTag: String): SFSpeechRecognizer? =
        SFSpeechRecognizer(locale = NSLocale(localeIdentifier = languageTag))

    private companion object {
        const val BUFFER_SIZE = 1024u
    }
}
