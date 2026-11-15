package com.kevin.memoverse.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.ui.theme.Hairline
import com.kevin.memoverse.ui.theme.InkMuted
import com.kevin.memoverse.ui.theme.Spacing

/** Bouton principal : pilule olive (variante claire pour les fonds sombres). */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.heightIn(min = 56.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            // Désactivé : fond filet et texte encre douce (4,2:1) ; l'ancien voile à 35 % tombait à 1,4:1.
            disabledContainerColor = Hairline,
            disabledContentColor = InkMuted,
        ),
        contentPadding = PaddingValues(horizontal = Spacing.xl, vertical = Spacing.lg),
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = Spacing.xxs, color = LocalContentColor.current)
        } else {
            // La typographie porte une couleur d'encre : on reprend celle du bouton (active ou désactivée).
            Text(text, style = MaterialTheme.typography.labelLarge, color = LocalContentColor.current)
        }
    }
}
