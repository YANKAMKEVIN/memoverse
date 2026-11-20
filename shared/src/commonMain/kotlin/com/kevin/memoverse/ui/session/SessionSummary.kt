package com.kevin.memoverse.ui.session

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.presentation.session.GrownVerseUi
import com.kevin.memoverse.presentation.session.SessionSummaryUi
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.scene_reussite
import com.kevin.memoverse.resources.session_continue
import com.kevin.memoverse.resources.session_summary_accuracy
import com.kevin.memoverse.resources.session_summary_accuracy_label
import com.kevin.memoverse.resources.session_summary_grown
import com.kevin.memoverse.resources.session_summary_encourage_subtitle
import com.kevin.memoverse.resources.session_summary_encourage_title
import com.kevin.memoverse.resources.session_summary_grown_title
import com.kevin.memoverse.resources.session_summary_wilted_title
import com.kevin.memoverse.resources.session_summary_subtitle
import com.kevin.memoverse.resources.session_summary_title
import com.kevin.memoverse.resources.session_summary_xp
import com.kevin.memoverse.resources.session_summary_xp_label
import com.kevin.memoverse.resources.session_summary_streak
import com.kevin.memoverse.resources.session_summary_xp_total
import com.kevin.memoverse.ui.components.PillButton
import com.kevin.memoverse.ui.components.stageName
import com.kevin.memoverse.ui.theme.AppTextStyles
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.Sand
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** Fin de séance : l'olivier en lumière, l'XP gagnée, la précision et les versets qui ont grandi. */
@Composable
fun SessionSummary(
    summary: SessionSummaryUi,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(Sand)) {
        Image(painterResource(Res.drawable.scene_reussite), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                // Fondu dès le haut du bloc : le titre ne se pose plus sur le rocher de l'illustration.
                .background(Brush.verticalGradient(0f to Sand.copy(alpha = 0f), 0.12f to Sand.copy(alpha = 0.92f), 1f to Sand))
                .padding(top = Spacing.xxl)
                .navigationBarsPadding()
                .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(
                text = stringResource(if (summary.isEncouragement) Res.string.session_summary_encourage_title else Res.string.session_summary_title),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = stringResource(if (summary.isEncouragement) Res.string.session_summary_encourage_subtitle else Res.string.session_summary_subtitle),
                style = AppTextStyles.displayItalic().copy(fontSize = MaterialTheme.typography.titleLarge.fontSize * 1.3f),
                textAlign = TextAlign.Center,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.padding(top = Spacing.sm)) {
                // Le cumul donne un sens aux points gagnés : « +60 XP · 420 au total ».
                val xpLabel = summary.totalXp?.let { stringResource(Res.string.session_summary_xp_total, it) } ?: stringResource(Res.string.session_summary_xp_label)
                StatTile(stringResource(Res.string.session_summary_xp, summary.xp), xpLabel, Modifier.weight(1f))
                StatTile(stringResource(Res.string.session_summary_accuracy, summary.accuracyPercent), stringResource(Res.string.session_summary_accuracy_label), Modifier.weight(1f))
            }
            if (summary.streakDays > 0) {
                Text(
                    text = pluralStringResource(Res.plurals.session_summary_streak, summary.streakDays, summary.streakDays),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StageChanges(Res.string.session_summary_grown_title, summary.grownVerses)
            StageChanges(Res.string.session_summary_wilted_title, summary.wiltedVerses)
            PillButton(text = stringResource(Res.string.session_continue), onClick = onContinue, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun StageChanges(title: StringResource, changes: List<GrownVerseUi>) {
    if (changes.isEmpty()) return
    val shape = RoundedCornerShape(Radius.card)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.6f), shape)
            .border(1.dp, Color.White.copy(alpha = 0.85f), shape)
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(stringResource(title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        changes.forEach { change ->
            Text(
                text = stringResource(Res.string.session_summary_grown, change.reference, stageName(change.from), stageName(change.to)),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(Radius.card)
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.6f), shape)
            .border(1.dp, Color.White.copy(alpha = 0.85f), shape)
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.headlineMedium)
    }
}
