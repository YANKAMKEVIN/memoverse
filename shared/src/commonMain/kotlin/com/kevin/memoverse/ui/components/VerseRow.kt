package com.kevin.memoverse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.presentation.themedetail.DifficultyUi
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.verse_learning
import com.kevin.memoverse.ui.icons.AppIcons
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.stringResource

/** Ligne de la liste d'un thème : case ronde, référence, début du verset, difficulté. */
@Composable
fun VerseRow(
    reference: String,
    text: String,
    difficulty: DifficultyUi,
    isSelected: Boolean,
    isLearning: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isLearning) LEARNING_ALPHA else 1f)
            .toggleable(value = isSelected, enabled = enabled && !isLearning, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(vertical = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Jardin plein : la case pâlit, pour qu'on ne la croie pas cochable.
        Box(Modifier.alpha(if (enabled || isLearning) 1f else DISABLED_MARK_ALPHA)) {
            SelectionMark(isSelected = isSelected, isLearning = isLearning)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                Text(reference, style = MaterialTheme.typography.titleLarge)
                if (isLearning) {
                    Text(
                        text = stringResource(Res.string.verse_learning),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(Radius.chip))
                            .padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
                    )
                }
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        DifficultyLeaves(difficulty)
    }
}

@Composable
private fun SelectionMark(isSelected: Boolean, isLearning: Boolean) {
    val colors = MaterialTheme.colorScheme
    when {
        isLearning -> Box(
            modifier = Modifier.size(28.dp).background(colors.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppIcons.Sprout, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
        }
        isSelected -> Box(
            modifier = Modifier.size(28.dp).background(colors.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            // La case est déjà annoncée « cochée » : pas de second « Sélectionné ».
            Icon(AppIcons.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(16.dp))
        }
        else -> Box(modifier = Modifier.size(28.dp).border(1.5.dp, colors.onSurfaceVariant.copy(alpha = 0.5f), CircleShape))
    }
}

/** Un verset déjà en apprentissage reste lisible mais n'est plus sélectionnable. */
private const val LEARNING_ALPHA = 0.6f
private const val DISABLED_MARK_ALPHA = 0.3f
