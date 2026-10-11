package com.kevin.memoverse.ui.session.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kevin.memoverse.presentation.session.ExerciseUi
import com.kevin.memoverse.presentation.session.SessionEvent
import com.kevin.memoverse.ui.components.AnswerOption
import com.kevin.memoverse.ui.theme.AppTextStyles
import com.kevin.memoverse.ui.theme.Spacing

@Composable
fun ChooseReferenceExercise(
    exercise: ExerciseUi.ChooseReference,
    isLocked: Boolean,
    onEvent: (SessionEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text("« ${exercise.verseText} »", style = AppTextStyles.verse())
        exercise.options.forEach { option ->
            AnswerOption(
                text = option.text,
                isSelected = option.isSelected,
                verdict = option.verdict,
                enabled = !isLocked,
                onClick = { onEvent(SessionEvent.OptionSelected(option.text)) },
            )
        }
    }
}
