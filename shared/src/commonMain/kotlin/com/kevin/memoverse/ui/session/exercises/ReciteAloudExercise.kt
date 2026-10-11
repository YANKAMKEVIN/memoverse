package com.kevin.memoverse.ui.session.exercises

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.presentation.session.ExerciseUi
import com.kevin.memoverse.presentation.session.SessionEvent
import com.kevin.memoverse.presentation.session.SpeechErrorUi
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.session_mic_button
import com.kevin.memoverse.resources.session_mic_listening
import com.kevin.memoverse.resources.session_mic_retry
import com.kevin.memoverse.resources.session_mic_start
import com.kevin.memoverse.resources.session_speech_no_match
import com.kevin.memoverse.resources.session_speech_other
import com.kevin.memoverse.resources.session_speech_language_pack
import com.kevin.memoverse.resources.session_speech_permission
import com.kevin.memoverse.resources.session_speech_unavailable
import com.kevin.memoverse.resources.session_type_instead
import com.kevin.memoverse.ui.icons.AppIcons
import com.kevin.memoverse.ui.permission.rememberMicrophonePermissionRequest
import com.kevin.memoverse.ui.theme.AppTextStyles
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.stringResource

/** Récitation orale : un grand micro, la transcription en direct, et « Écrire plutôt » en secours. */
@Composable
fun ReciteAloudExercise(
    exercise: ExerciseUi.ReciteAloud,
    isLocked: Boolean,
    onEvent: (SessionEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (exercise.isTyping) {
        TypeExercise(
            exercise = ExerciseUi.Type(exercise.reference, null, "", "", "", exercise.transcript),
            isLocked = isLocked,
            onEvent = onEvent,
            modifier = modifier,
        )
        return
    }
    val requestMicrophone = rememberMicrophonePermissionRequest { granted ->
        onEvent(if (granted) SessionEvent.MicClicked else SessionEvent.MicPermissionDenied)
    }
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .border(1.dp, colors.outline, RoundedCornerShape(Radius.card))
                .padding(Spacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            if (exercise.transcript.isNotBlank()) {
                Text(exercise.transcript, style = AppTextStyles.verse(), textAlign = TextAlign.Center)
            }
        }
        MicButton(
            isListening = exercise.isListening,
            enabled = !isLocked,
            onClick = { if (exercise.isListening) onEvent(SessionEvent.MicClicked) else requestMicrophone() },
        )
        Text(
            text = stringResource(
                when {
                    exercise.isListening -> Res.string.session_mic_listening
                    exercise.transcript.isNotBlank() -> Res.string.session_mic_retry
                    else -> Res.string.session_mic_start
                },
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
        // Le secours reste juste sous le micro, visible même quand un message d'erreur s'affiche.
        Text(
            text = stringResource(Res.string.session_type_instead),
            style = MaterialTheme.typography.titleMedium,
            color = colors.primary,
            modifier = Modifier.clickable(enabled = !isLocked) { onEvent(SessionEvent.TypeInsteadClicked) }.padding(Spacing.sm),
        )
        exercise.error?.let { error ->
            Text(
                text = stringResource(
                    when (error) {
                        SpeechErrorUi.PERMISSION_DENIED -> Res.string.session_speech_permission
                        SpeechErrorUi.UNAVAILABLE -> Res.string.session_speech_unavailable
                        SpeechErrorUi.LANGUAGE_PACK_MISSING -> Res.string.session_speech_language_pack
                        SpeechErrorUi.NO_MATCH -> Res.string.session_speech_no_match
                        SpeechErrorUi.OTHER -> Res.string.session_speech_other
                    },
                ),
                style = MaterialTheme.typography.bodySmall,
                color = colors.error,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MicButton(isListening: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val pulse by rememberInfiniteTransition(label = "micro").animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) PULSE_SCALE else 1f,
        animationSpec = infiniteRepeatable(tween(PULSE_MILLIS), RepeatMode.Reverse),
        label = "pulsation",
    )
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(MIC_SIZE)
                .scale(pulse)
                .background(colors.primary.copy(alpha = if (isListening) 0.18f else 0f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(MIC_SIZE - 24.dp)
                .background(if (isListening) colors.error else colors.primary, CircleShape)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isListening) AppIcons.Stop else AppIcons.Microphone,
                contentDescription = stringResource(Res.string.session_mic_button),
                tint = colors.onPrimary,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

private val MIC_SIZE = 112.dp
private const val PULSE_SCALE = 1.25f
private const val PULSE_MILLIS = 700
