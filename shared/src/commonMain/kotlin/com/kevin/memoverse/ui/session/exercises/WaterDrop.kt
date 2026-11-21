package com.kevin.memoverse.ui.session.exercises

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.ui.theme.Water
import com.kevin.memoverse.ui.theme.WaterLight
import kotlin.math.PI
import kotlin.math.sin
import com.kevin.memoverse.ui.components.rememberReduceMotion

/**
 * La goutte de l'oral : elle se remplit d'eau à chaque mot récité ([fill] de 0 à 1).
 * Pendant l'écoute, la surface ondule plus fort et la goutte respire.
 */
@Composable
fun WaterDrop(
    fill: Float,
    isListening: Boolean,
    enabled: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val level by animateFloatAsState(fill, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow), label = "niveau")
    val transition = rememberInfiniteTransition(label = "eau")
    val phase by transition.animateFloat(0f, 2f * PI.toFloat(), infiniteRepeatable(tween(WAVE_MILLIS, easing = LinearEasing)), label = "vague")
    val breath by transition.animateFloat(1f, BREATH_SCALE, infiniteRepeatable(tween(BREATH_MILLIS), androidx.compose.animation.core.RepeatMode.Reverse), label = "souffle")
    val reduceMotion = rememberReduceMotion()
    val amplitude by animateFloatAsState(if (reduceMotion) 0f else if (isListening) LISTENING_AMPLITUDE else IDLE_AMPLITUDE, label = "amplitude")
    // Une impulsion quand la goutte devient pleine : la récitation est réussie.
    val haptic = LocalHapticFeedback.current
    val isFull = fill >= 1f
    var wasFull by remember { mutableStateOf(isFull) }
    LaunchedEffect(isFull) {
        if (isFull && !wasFull) haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
        wasFull = isFull
    }
    val outline by animateColorAsState(if (fill >= 1f) colors.primary else colors.outline, label = "contour")
    Canvas(
        modifier = modifier
            .size(DROP_WIDTH, DROP_HEIGHT)
            .scale(if (isListening && !reduceMotion) breath else 1f)
            .semantics { contentDescription = label }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        val drop = dropPath(size)
        drawPath(drop, colors.surface)
        clipPath(drop) {
            // Toujours un fond d'eau visible, pour qu'on comprenne d'emblée ce qui va se remplir.
            val surfaceY = size.height * (1f - (MIN_LEVEL + (1f - MIN_LEVEL - TOP_GAP) * level))
            val wave = Path().apply {
                moveTo(0f, size.height)
                var x = 0f
                while (x <= size.width) {
                    val y = surfaceY + sin(x / size.width * 2f * PI.toFloat() * WAVES + phase) * amplitude * density
                    lineTo(x, y)
                    x += 4f
                }
                lineTo(size.width, size.height)
                close()
            }
            drawPath(wave, Brush.verticalGradient(listOf(WaterLight, Water), startY = surfaceY, endY = size.height))
            // Reflet clair en haut à gauche, comme une goutte de rosée.
            drawOval(
                color = Color.White.copy(alpha = 0.35f),
                topLeft = Offset(size.width * 0.26f, size.height * 0.42f),
                size = Size(size.width * 0.1f, size.height * 0.18f),
            )
        }
        drawPath(drop, outline, style = Stroke(width = 2.dp.toPx()))
    }
}

/** Goutte : pointe en haut, ventre rond en bas. */
private fun dropPath(size: Size): Path = Path().apply {
    val w = size.width
    val h = size.height
    moveTo(w / 2f, h * 0.02f)
    cubicTo(w * 0.7f, h * 0.28f, w * 0.96f, h * 0.46f, w * 0.96f, h * 0.64f)
    cubicTo(w * 0.96f, h * 0.84f, w * 0.75f, h * 0.98f, w / 2f, h * 0.98f)
    cubicTo(w * 0.25f, h * 0.98f, w * 0.04f, h * 0.84f, w * 0.04f, h * 0.64f)
    cubicTo(w * 0.04f, h * 0.46f, w * 0.3f, h * 0.28f, w / 2f, h * 0.02f)
    close()
}

private val DROP_WIDTH = 108.dp
private val DROP_HEIGHT = 134.dp
private const val MIN_LEVEL = 0.06f
private const val TOP_GAP = 0.04f
private const val WAVES = 1.4f
private const val IDLE_AMPLITUDE = 2.5f
private const val LISTENING_AMPLITUDE = 6f
private const val WAVE_MILLIS = 2200
private const val BREATH_MILLIS = 900
private const val BREATH_SCALE = 1.03f
