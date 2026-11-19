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
import com.kevin.memoverse.ui.theme.Leaf
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.stringResource

/**
 * Proposition de réponse ; sélectionnée, elle passe en sauge.
 * Après validation, le verdict est aussi porté par une icône (coche / croix) et annoncé aux lecteurs d'écran :
 * la couleur seule ne suffit pas.
 */
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
        // Feuille plutôt que sauge : le texte blanc passe de 3,0:1 à 4,5:1.
        null -> if (isSelected) Leaf else null
    }
    val verdictLabel = when (verdict) {
        OptionVerdict.CORRECT -> stringResource(Res.string.session_option_correct)
        OptionVerdict.WRONG_CHOICE -> stringResource(Res.string.session_option_wrong)
        null -> null
    }
    val contentColor = if (highlight != null) Color.White else colors.onSurface
    Row(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoView)
            .background(highlight ?: Color.White.copy(alpha = 0.7f), shape)
            .border(1.dp, highlight ?: colors.outline, shape)
            .selectable(selected = isSelected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .semantics { verdictLabel?.let { stateDescription = it } }
            .padding(horizontal = Spacing.lg + Spacing.xxs, vertical = Spacing.md + Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight != null) FontWeight.Medium else FontWeight.Normal,
            color = contentColor,
            modifier = Modifier.weight(1f),
        )
        if (verdict != null) {
            Icon(
                imageVector = if (verdict == OptionVerdict.CORRECT) AppIcons.Check else AppIcons.Cross,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
