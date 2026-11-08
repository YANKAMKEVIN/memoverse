package com.kevin.memoverse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.presentation.session.OptionVerdict
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.session_option_correct
import com.kevin.memoverse.resources.session_option_wrong
import com.kevin.memoverse.ui.icons.AppIcons
import com.kevin.memoverse.ui.theme.LeafDeep
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.stringResource

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
    // Après validation, la réponse choisie revient à l'écran : le panneau de correction a pu la pousser hors de vue.
    val bringIntoView = remember { BringIntoViewRequester() }
    LaunchedEffect(verdict != null && isSelected) {
        if (verdict != null && isSelected) {
            withFrameNanos {}
            bringIntoView.bringIntoView()
        }
    }
    // Après validation : la bonne réponse en vert, le mauvais choix en terre cuite.
    val highlight = when (verdict) {
        OptionVerdict.CORRECT -> colors.primary
        OptionVerdict.WRONG_CHOICE -> colors.error
        null -> if (isSelected) LeafDeep else null
    }
    // Le verdict ne repose pas que sur la couleur : une icône à l'écran, un état pour le lecteur d'écran.
    val verdictLabel = verdict?.let { stringResource(if (it == OptionVerdict.CORRECT) Res.string.session_option_correct else Res.string.session_option_wrong) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoView)
            .background(highlight ?: Color.White.copy(alpha = 0.7f), shape)
            .border(1.dp, highlight ?: colors.outline, shape)
            .selectable(selected = isSelected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .semantics { if (verdictLabel != null) stateDescription = verdictLabel }
            .padding(horizontal = Spacing.lg + Spacing.xxs, vertical = Spacing.md + Spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight != null) FontWeight.Medium else FontWeight.Normal,
            color = if (highlight != null) Color.White else colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        verdict?.let {
            Icon(if (it == OptionVerdict.CORRECT) AppIcons.Check else AppIcons.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}
