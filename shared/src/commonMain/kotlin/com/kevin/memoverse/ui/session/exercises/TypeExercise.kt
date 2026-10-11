package com.kevin.memoverse.ui.session.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.presentation.session.ExerciseUi
import com.kevin.memoverse.presentation.session.SessionEvent
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.session_type_placeholder
import com.kevin.memoverse.ui.components.VerseWithHiddenWords
import com.kevin.memoverse.ui.theme.AppTextStyles
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.stringResource

/** Écrire la partie cachée, ou tout le verset quand [ExerciseUi.Type.position] est nulle. */
@Composable
fun TypeExercise(
    exercise: ExerciseUi.Type,
    isLocked: Boolean,
    onEvent: (SessionEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        if (exercise.position != null) {
            VerseWithHiddenWords(before = exercise.before, hidden = exercise.hidden, after = exercise.after)
        }
        OutlinedTextField(
            value = exercise.text,
            onValueChange = { onEvent(SessionEvent.TextChanged(it)) },
            enabled = !isLocked,
            placeholder = { Text(stringResource(Res.string.session_type_placeholder), style = AppTextStyles.verse()) },
            textStyle = AppTextStyles.verse(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, autoCorrectEnabled = false),
            shape = RoundedCornerShape(Radius.card),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
            modifier = Modifier.fillMaxWidth().heightIn(min = if (exercise.position == null) 180.dp else 110.dp),
        )
    }
}
