package com.kevin.memoverse.data.speech

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import com.kevin.memoverse.domain.speech.SpeechError
import com.kevin.memoverse.domain.speech.SpeechEvent
import com.kevin.memoverse.domain.speech.SpeechRecognitionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

/**
 * Reconnaissance vocale Android, sur l'appareil quand c'est possible (Android 12+). Si le pack
 * français manque, on demande à Android de le télécharger et on retombe en attendant sur le moteur
 * standard : d'abord hors-ligne, puis en ligne si le hors-ligne n'a pas le français non plus.
 */
class AndroidSpeechRecognitionService(
    private val context: Context,
) : SpeechRecognitionService {

    private var current: SpeechRecognizer? = null

    override val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context) || isOnDeviceAvailable()

    override fun listen(languageTag: String): Flow<SpeechEvent> = callbackFlow {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            trySend(SpeechEvent.Failed(SpeechError.PERMISSION_DENIED))
            close()
            awaitClose()
            return@callbackFlow
        }

        // Une fois le pack absent constaté, l'échec final l'explique au lieu d'un message vague.
        var languageMissingSeen = false

        fun start(mode: Mode) {
            val onDevice = mode == Mode.ON_DEVICE
            val recognizer = if (onDevice && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            } else {
                SpeechRecognizer.createSpeechRecognizer(context)
            }
            current = recognizer
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    trySend(SpeechEvent.Listening)
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    partialResults.bestText()?.let { trySend(SpeechEvent.Partial(it)) }
                }

                override fun onResults(results: Bundle?) {
                    trySend(results.bestText()?.let { SpeechEvent.Final(it) } ?: SpeechEvent.Failed(SpeechError.NO_MATCH))
                    close()
                }

                override fun onError(error: Int) {
                    val languageMissing = error == SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED || error == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE
                    val next = mode.fallback()
                    if (languageMissing) languageMissingSeen = true
                    if (languageMissing && next != null) {
                        if (onDevice && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !downloadRequested) {
                            // Android propose le téléchargement une seule fois par lancement de l'app.
                            downloadRequested = true
                            runCatching { recognizer.triggerModelDownload(recognizerIntent(languageTag, preferOffline = true)) }
                        }
                        recognizer.destroy()
                        start(next)
                        return
                    }
                    val failure = if (languageMissingSeen && error != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                        SpeechError.LANGUAGE_PACK_MISSING
                    } else {
                        error.toSpeechError()
                    }
                    trySend(SpeechEvent.Failed(failure))
                    close()
                }

                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
            recognizer.startListening(recognizerIntent(languageTag, preferOffline = mode != Mode.STANDARD_ONLINE))
        }

        start(if (isOnDeviceAvailable()) Mode.ON_DEVICE else Mode.STANDARD_OFFLINE)
        awaitClose {
            current?.destroy()
            current = null
        }
    }.flowOn(Dispatchers.Main)

    override fun stop() {
        current?.stopListening()
    }

    private companion object {
        @Volatile
        var downloadRequested = false
    }

    private enum class Mode {
        ON_DEVICE,
        STANDARD_OFFLINE,
        STANDARD_ONLINE;

        fun fallback(): Mode? = entries.getOrNull(ordinal + 1)
    }

    private fun recognizerIntent(languageTag: String, preferOffline: Boolean): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            .putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, preferOffline)

    private fun isOnDeviceAvailable(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

    private fun Bundle?.bestText(): String? =
        this?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.takeIf { it.isNotBlank() }

    private fun Int.toSpeechError(): SpeechError = when (this) {
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> SpeechError.PERMISSION_DENIED
        SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> SpeechError.NO_MATCH
        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED, SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> SpeechError.LANGUAGE_PACK_MISSING
        else -> SpeechError.OTHER
    }
}
