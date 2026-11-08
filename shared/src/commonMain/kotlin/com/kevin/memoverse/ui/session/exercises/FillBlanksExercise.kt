package com.kevin.memoverse.ui.session.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.presentation.session.ExerciseUi
import com.kevin.memoverse.presentation.session.SegmentUi
import com.kevin.memoverse.presentation.session.SessionEvent
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.session_blank_empty
import com.kevin.memoverse.resources.session_blank_filled
import com.kevin.memoverse.ui.components.WordPebble
import com.kevin.memoverse.ui.theme.AppTextStyles
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FillBlanksExercise(
    exercise: ExerciseUi.FillBlanks,
    isLocked: Boolean,
    onEvent: (SessionEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = AppTextStyles.verse()
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xl)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            exercise.segments.forEach { segment ->
                when (segment) {
                    is SegmentUi.Word -> Text(segment.text, style = style)
                    is SegmentUi.Blank -> {
                    val label = segment.filledWord?.let { stringResource(Res.string.session_blank_filled, segment.slot + 1, it) }
                        ?: stringResource(Res.string.session_blank_empty, segment.slot + 1)
                    Box(
                        modifier = Modifier
                            .widthIn(min = 72.dp)
                            .heightIn(min = 48.dp)
                            .background(colors.primaryContainer.copy(alpha = if (segment.filledWord != null) 1f else 0.4f), RoundedCornerShape(Radius.chip))
                            .drawBehind {
                                drawLine(colors.secondary, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx())
                            }
                            .clickable(enabled = !isLocked && segment.filledWord != null, role = Role.Button) { onEvent(SessionEvent.BlankTapped(segment.slot)) }
                            .semantics { contentDescription = label }
                            .padding(horizontal = Spacing.sm),
                        contentAlignment = Alignment.Center,
                    ) {
                        segment.filledWord?.let { Text(it, style = style, color = colors.primary) }
                    }
                    }
                }
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            exercise.choices.forEach { choice ->
                WordPebble(
                    text = choice.text,
                    enabled = !isLocked && !choice.isUsed,
                    onClick = { onEvent(SessionEvent.WordChoiceTapped(choice.index)) },
                )
            }
        }
    }
}
