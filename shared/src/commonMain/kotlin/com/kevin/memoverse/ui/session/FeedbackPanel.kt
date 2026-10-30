package com.kevin.memoverse.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.presentation.session.FeedbackUi
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.session_continue
import com.kevin.memoverse.resources.session_feedback_answer
import com.kevin.memoverse.resources.session_feedback_correct
import com.kevin.memoverse.resources.session_feedback_grew
import com.kevin.memoverse.resources.session_feedback_incorrect
import com.kevin.memoverse.resources.session_feedback_wilted
import com.kevin.memoverse.ui.components.PillButton
import com.kevin.memoverse.ui.components.stageName
import com.kevin.memoverse.ui.icons.AppIcons
import com.kevin.memoverse.ui.theme.AppTextStyles
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.stringResource

/** Bandeau après « Valider » : sauge si c'est juste, terre cuite douce sinon. */
@Composable
fun FeedbackPanel(
    feedback: FeedbackUi,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val accent = if (feedback.isCorrect) colors.primary else colors.error
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (feedback.isCorrect) colors.primaryContainer else ErrorContainer, RoundedCornerShape(topStart = Radius.sheet, topEnd = Radius.sheet))
            .navigationBarsPadding()
            .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        // Le texte défile si le panneau manque de place (grande police, long verset) ; « Continuer » reste visible.
        Column(
            modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(accent, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(if (feedback.isCorrect) AppIcons.Check else AppIcons.Sprout, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                    Text(
                        text = stringResource(if (feedback.isCorrect) Res.string.session_feedback_correct else Res.string.session_feedback_incorrect),
                        style = MaterialTheme.typography.headlineMedium,
                        color = accent,
                    )
                    val growth = when {
                        feedback.grewTo != null -> stringResource(Res.string.session_feedback_grew, feedback.reference, stageName(feedback.grewTo))
                        feedback.wiltedTo != null -> stringResource(Res.string.session_feedback_wilted, feedback.reference, stageName(feedback.wiltedTo))
                        else -> null
                    }
                    growth?.let { Text(it, style = MaterialTheme.typography.titleSmall, color = accent) }
                }
            }
            if (!feedback.isCorrect) {
                Text(stringResource(Res.string.session_feedback_answer), style = MaterialTheme.typography.titleSmall, color = colors.onSurfaceVariant)
                feedback.expectedAnswer?.let { Text(it, style = MaterialTheme.typography.titleLarge, color = accent) }
            }
            // L'exercice affiche déjà le verset après validation ; on ne le répète que s'il est la correction.
            if (!feedback.isCorrect && feedback.expectedAnswer == null) {
                Text("« ${feedback.verseText} »", style = AppTextStyles.verseQuote())
                Text(feedback.reference, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
        }
        PillButton(
            text = stringResource(Res.string.session_continue),
            onClick = onContinue,
            containerColor = accent,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private val ErrorContainer = Color(0xFFF3DED5)
