package com.kevin.memoverse.ui.session.exercises

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.presentation.session.ExerciseUi
import com.kevin.memoverse.presentation.session.SessionEvent
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.session_reorder_placeholder
import com.kevin.memoverse.ui.components.WordPebble
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReorderExercise(
    exercise: ExerciseUi.Reorder,
    isLocked: Boolean,
    onEvent: (SessionEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xl)) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Radius.card))
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (exercise.placed.isEmpty()) {
                Text(
                    text = stringResource(Res.string.session_reorder_placeholder),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(Spacing.sm),
                )
            }
            exercise.placed.forEach { chunk ->
                WordPebble(text = chunk.text, enabled = !isLocked, onClick = { onEvent(SessionEvent.ChunkRemoved(chunk.index)) })
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            exercise.available.forEach { chunk ->
                WordPebble(text = chunk.text, enabled = !isLocked, onClick = { onEvent(SessionEvent.ChunkPlaced(chunk.index)) })
            }
        }
    }
}
