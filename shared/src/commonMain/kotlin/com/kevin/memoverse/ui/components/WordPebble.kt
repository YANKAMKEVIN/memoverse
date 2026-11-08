package com.kevin.memoverse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.ui.theme.AppTextStyles
import com.kevin.memoverse.ui.theme.Spacing

/** Galet gravé d'un mot (exercices « remettre en ordre » et « mots manquants »). */
@Composable
fun WordPebble(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(percent = 50)
    Text(
        text = text,
        style = AppTextStyles.verse().copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize * 1.05f),
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .shadow(if (enabled) 3.dp else 0.dp, shape, ambientColor = PebbleShadow, spotColor = PebbleShadow)
            .background(Brush.verticalGradient(listOf(PebbleTop, PebbleBottom)), shape)
            .border(1.dp, Color.White.copy(alpha = 0.7f), shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
    )
}

private val PebbleTop = Color(0xFFF7F0E4)
private val PebbleBottom = Color(0xFFE6DAC6)
private val PebbleShadow = Color(0x55594630)
private const val DISABLED_ALPHA = 0.3f
