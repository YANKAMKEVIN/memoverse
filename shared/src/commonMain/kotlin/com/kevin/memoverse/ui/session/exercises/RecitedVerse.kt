package com.kevin.memoverse.ui.session.exercises

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.presentation.session.RecitedWordState
import com.kevin.memoverse.presentation.session.RecitedWordUi
import com.kevin.memoverse.ui.theme.AppTextStyles

/**
 * Le verset mot à mot : un mot pas encore dit garde sa place (pointillés à la largeur du mot),
 * puis apparaît en douceur quand il est entendu. La ligne ne bouge donc jamais pendant la récitation.
 */
@Composable
fun RecitedVerse(words: List<RecitedWordUi>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        words.forEach { RecitedWord(it) }
    }
}

@Composable
private fun RecitedWord(word: RecitedWordUi) {
    val colors = MaterialTheme.colorScheme
    val isShown = word.state != RecitedWordState.HIDDEN
    val color by animateColorAsState(
        targetValue = when (word.state) {
            RecitedWordState.HIDDEN -> Color.Transparent
            RecitedWordState.RECITED -> colors.onSurface
            RecitedWordState.MISSED -> colors.error
        },
        animationSpec = tween(REVEAL_MILLIS),
        label = "mot",
    )
    val rise by animateFloatAsState(if (isShown) 0f else 1f, tween(REVEAL_MILLIS), label = "montée")
    val dots = colors.outline
    Text(
        text = word.text,
        style = AppTextStyles.verse(),
        color = color,
        modifier = Modifier
            // Un mot caché ne doit pas être lu par l'accessibilité : ce serait souffler la réponse.
            .then(if (isShown) Modifier else Modifier.clearAndSetSemantics { })
            .graphicsLayer { translationY = rise * 6.dp.toPx() }
            .drawBehind {
                if (!isShown) {
                    val y = size.height * 0.72f
                    drawLine(
                        color = dots,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 3.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(0.1f, 7.dp.toPx())),
                    )
                }
            },
    )
}

private const val REVEAL_MILLIS = 420
