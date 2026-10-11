package com.kevin.memoverse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.presentation.themedetail.DifficultyUi
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.difficulty_easy
import com.kevin.memoverse.resources.difficulty_hard
import com.kevin.memoverse.resources.difficulty_medium
import com.kevin.memoverse.ui.theme.Leaf
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.stringResource

/** Difficulté d'un verset : 1 à 3 feuilles pleines, avec le libellé dessous. */
@Composable
fun DifficultyLeaves(
    difficulty: DifficultyUi,
    modifier: Modifier = Modifier,
) {
    val (filled, label) = when (difficulty) {
        DifficultyUi.EASY -> 1 to Res.string.difficulty_easy
        DifficultyUi.MEDIUM -> 2 to Res.string.difficulty_medium
        DifficultyUi.HARD -> 3 to Res.string.difficulty_hard
    }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            repeat(3) { index -> LeafShape(isFilled = index < filled) }
        }
        Text(stringResource(label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LeafShape(isFilled: Boolean) {
    Canvas(modifier = Modifier.size(width = 9.dp, height = 13.dp)) {
        rotate(degrees = -30f) {
            val leafSize = Size(size.width * 0.8f, size.height)
            val topLeft = Offset((size.width - leafSize.width) / 2f, 0f)
            if (isFilled) drawOval(Leaf, topLeft, leafSize) else drawOval(Leaf, topLeft, leafSize, style = Stroke(width = 1.dp.toPx()))
        }
    }
}
