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
import androidx.compose.ui.text.style.TextAlign
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.loading_error
import com.kevin.memoverse.resources.retry
import com.kevin.memoverse.resources.session_back_to_garden
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.stringResource

@Composable
fun ErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.loading_error),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        PillButton(text = stringResource(Res.string.retry), onClick = onRetry)
        if (onClose != null) TextButton(onClick = onClose) { Text(stringResource(Res.string.session_back_to_garden)) }
    }
}
