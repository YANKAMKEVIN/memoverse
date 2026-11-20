package com.kevin.memoverse.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.loading_error
import com.kevin.memoverse.resources.retry
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.stringResource

/**
 * Erreur de chargement : un message, « Réessayer », et si besoin un titre et une sortie
 * ([secondaryAction], par exemple « Retour au jardin ») pour ne jamais laisser dans une impasse.
 */
@Composable
fun ErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    message: String = stringResource(Res.string.loading_error),
    secondaryAction: Pair<String, () -> Unit>? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            title?.let { Text(it, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center) }
            Text(
                text = message,
                style = if (title == null) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                color = if (title == null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        PillButton(text = stringResource(Res.string.retry), onClick = onRetry)
        secondaryAction?.let { (label, action) ->
            TextButton(onClick = action) { Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface) }
        }
    }
}
