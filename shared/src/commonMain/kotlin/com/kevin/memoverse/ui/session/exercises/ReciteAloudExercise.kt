package com.kevin.memoverse.ui.session.exercises

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.presentation.session.ExerciseUi
import com.kevin.memoverse.presentation.session.SessionEvent
import com.kevin.memoverse.presentation.session.SpeechErrorUi
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.session_drop_full
import com.kevin.memoverse.resources.session_drop_idle
import com.kevin.memoverse.resources.session_drop_label
import com.kevin.memoverse.resources.session_drop_listening
import com.kevin.memoverse.resources.session_drop_paused
import com.kevin.memoverse.resources.session_drop_start
import com.kevin.memoverse.resources.session_drop_stop
import com.kevin.memoverse.resources.session_mic_button
import com.kevin.memoverse.resources.session_speech_language_pack
import com.kevin.memoverse.resources.session_speech_no_match
import com.kevin.memoverse.resources.session_speech_other
import com.kevin.memoverse.resources.session_speech_permission
import com.kevin.memoverse.resources.session_speech_unavailable
import com.kevin.memoverse.resources.session_type_instead
import com.kevin.memoverse.resources.session_speak_instead
import com.kevin.memoverse.ui.icons.AppIcons
import com.kevin.memoverse.ui.permission.rememberMicrophonePermissionRequest
import com.kevin.memoverse.ui.theme.AppTextStyles
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * Récitation orale « La goutte » : le verset se révèle mot à mot pendant qu'on le dit,
 * et la goutte se remplit ; pleine, la récitation est réussie.
 */
@Composable
fun ReciteAloudExercise(
    exercise: ExerciseUi.ReciteAloud,
    isLocked: Boolean,
    onEvent: (SessionEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (exercise.isTyping) {
        Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            TypeExercise(
                exercise = ExerciseUi.Type(exercise.reference, null, "", "", "", exercise.transcript),
                isLocked = isLocked,
                onEvent = onEvent,
            )
            if (!isLocked) {
                Text(
                    text = stringResource(Res.string.session_speak_instead),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onEvent(SessionEvent.SpeakInsteadClicked) }.padding(Spacing.sm),
                )
            }
        }
        return
    }
    val requestMicrophone = rememberMicrophonePermissionRequest { granted ->
        onEvent(if (granted) SessionEvent.MicClicked else SessionEvent.MicPermissionDenied)
    }
    val toggle = { if (exercise.isListening) onEvent(SessionEvent.MicClicked) else requestMicrophone() }
    val colors = MaterialTheme.colorScheme
    val isFull = exercise.fill >= 1f
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        RecitedVerse(words = exercise.words)
        // Goutte à gauche, consigne et actions à droite : tout l'exercice tient au-dessus de « Valider ».
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            WaterDrop(
                fill = exercise.fill,
                isListening = exercise.isListening,
                enabled = !isLocked,
                label = stringResource(Res.string.session_drop_label, (exercise.fill * 100).roundToInt()),
                onClick = toggle,
            )
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = stringResource(
                        when {
                            isFull -> Res.string.session_drop_full
                            exercise.isListening -> Res.string.session_drop_listening
                            exercise.transcript.isNotBlank() -> Res.string.session_drop_paused
                            else -> Res.string.session_drop_idle
                        },
                    ),
                    style = AppTextStyles.verseQuote(),
                    color = colors.onSurfaceVariant,
                )
                if (!isLocked) {
                    if (!isFull) ListenButton(isListening = exercise.isListening, onClick = toggle)
                    Text(
                        text = stringResource(Res.string.session_type_instead),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.primary,
                        modifier = Modifier.clickable { onEvent(SessionEvent.TypeInsteadClicked) }.padding(vertical = Spacing.xs),
                    )
                }
            }
        }
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

/** « Réciter » (micro) ou « J'ai fini » (point rouge qui pulse) pendant l'écoute. */
@Composable
private fun ListenButton(isListening: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val pulse by rememberInfiniteTransition(label = "enregistrement").animateFloat(
        initialValue = 1f,
        targetValue = PULSE_ALPHA,
        animationSpec = infiniteRepeatable(tween(PULSE_MILLIS), RepeatMode.Reverse),
        label = "pulsation",
    )
    Row(
        modifier = Modifier
            .heightIn(min = 52.dp)
            .background(colors.primary, CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Spacing.xl),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (isListening) {
            Box(Modifier.size(10.dp).alpha(pulse).background(colors.error, CircleShape))
        } else {
            Icon(AppIcons.Microphone, contentDescription = stringResource(Res.string.session_mic_button), tint = colors.onPrimary, modifier = Modifier.size(20.dp))
        }
        Text(
            text = stringResource(if (isListening) Res.string.session_drop_stop else Res.string.session_drop_start),
            style = MaterialTheme.typography.labelLarge,
            color = colors.onPrimary,
        )
    }
}

private const val PULSE_ALPHA = 0.25f
private const val PULSE_MILLIS = 650
