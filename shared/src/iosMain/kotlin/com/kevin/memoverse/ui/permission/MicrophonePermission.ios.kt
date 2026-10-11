package com.kevin.memoverse.ui.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import platform.AVFAudio.AVAudioSession
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberMicrophonePermissionRequest(onResult: (granted: Boolean) -> Unit): () -> Unit {
    val currentOnResult by rememberUpdatedState(onResult)
    return remember {
        {
            AVAudioSession.sharedInstance().requestRecordPermission { micGranted ->
                if (!micGranted) {
                    dispatch_async(dispatch_get_main_queue()) { currentOnResult(false) }
                } else {
                    SFSpeechRecognizer.requestAuthorization { status ->
                        val granted = status == SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized
                        dispatch_async(dispatch_get_main_queue()) { currentOnResult(granted) }
                    }
                }
            }
        }
    }
}
