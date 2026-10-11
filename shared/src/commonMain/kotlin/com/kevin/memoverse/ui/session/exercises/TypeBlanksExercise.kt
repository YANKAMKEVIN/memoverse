package com.kevin.memoverse.ui.session.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.presentation.session.ExerciseUi
import com.kevin.memoverse.presentation.session.SessionEvent
import com.kevin.memoverse.presentation.session.TypedSegmentUi
import com.kevin.memoverse.ui.theme.AppTextStyles
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.Spacing

/** Le verset avec des trous à remplir au clavier, sans propositions. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TypeBlanksExercise(
    exercise: ExerciseUi.TypeBlanks,
    isLocked: Boolean,
    onEvent: (SessionEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = AppTextStyles.verse()
    val colors = MaterialTheme.colorScheme
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        exercise.segments.forEach { segment ->
            when (segment) {
                is TypedSegmentUi.Word -> Text(segment.text, style = style)
                is TypedSegmentUi.Blank -> BasicTextField(
                    value = segment.value,
                    onValueChange = { onEvent(SessionEvent.BlankTextChanged(segment.slot, it)) },
                    enabled = !isLocked,
                    singleLine = true,
                    textStyle = style.copy(color = colors.primary, textAlign = TextAlign.Center),
                    cursorBrush = SolidColor(colors.primary),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier
                        .width((BLANK_WIDTH_PER_LETTER * segment.length).coerceAtLeast(MIN_BLANK_WIDTH))
                        .background(colors.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(Radius.chip))
                        .drawBehind {
                            drawLine(colors.secondary, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx())
                        }
                        .padding(horizontal = Spacing.xs, vertical = Spacing.xxs),
                    decorationBox = { inner -> Box(contentAlignment = Alignment.Center) { inner() } },
                )
            }
        }
    }
}

private val BLANK_WIDTH_PER_LETTER = 13.dp
private val MIN_BLANK_WIDTH = 64.dp
