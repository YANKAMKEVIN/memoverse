package com.kevin.memoverse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.presentation.session.OptionVerdict
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.Spacing

/** Proposition de réponse ; sélectionnée, elle passe en sauge. */
@Composable
fun AnswerOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    verdict: OptionVerdict? = null,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(Radius.row)
    // Après validation : la bonne réponse en vert, le mauvais choix en terre cuite.
    val highlight = when (verdict) {
        OptionVerdict.CORRECT -> colors.primary
        OptionVerdict.WRONG_CHOICE -> colors.error
        null -> if (isSelected) colors.secondary else null
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (highlight != null) FontWeight.Medium else FontWeight.Normal,
        color = if (highlight != null) Color.White else colors.onSurface,
        modifier = modifier
            .fillMaxWidth()
            .background(highlight ?: Color.White.copy(alpha = 0.7f), shape)
            .border(1.dp, highlight ?: colors.outline, shape)
            .selectable(selected = isSelected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = Spacing.lg + Spacing.xxs, vertical = Spacing.md + Spacing.xxs),
    )
}
