package com.kevin.memoverse.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kevin.memoverse.ui.theme.Cream
import com.kevin.memoverse.ui.theme.Ink
import com.kevin.memoverse.ui.theme.InkMuted

/**
 * Boîte de décision aux couleurs du jardin : fond crème, titre en Fraunces, action principale en
 * pilule olive (terre cuite si elle détruit quelque chose), action secondaire en texte encre.
 */
@Composable
fun GardenDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = Cream,
        titleContentColor = Ink,
        textContentColor = InkMuted,
        title = { Text(title, style = MaterialTheme.typography.headlineMedium) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium, color = InkMuted) },
        confirmButton = {
            PillButton(
                text = confirmText,
                onClick = onConfirm,
                containerColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                // Blanc sur terre cuite : contraste suffisant (le crème du thème est juste sous AA).
                contentColor = if (isDestructive) Color.White else MaterialTheme.colorScheme.onPrimary,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissText, style = MaterialTheme.typography.labelLarge, color = Ink) }
        },
    )
}
