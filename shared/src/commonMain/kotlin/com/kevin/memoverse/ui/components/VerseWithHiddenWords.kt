package com.kevin.memoverse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.ui.theme.AppTextStyles
import com.kevin.memoverse.ui.theme.Spacing
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.session_hidden_word
import org.jetbrains.compose.resources.stringResource

/**
 * Verset dont une partie est cachée sous des « galets de brume » : une forme par mot, de la
 * longueur du mot. Le texte caché n'est jamais rendu (un flou ne fonctionne pas sous Android 12).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VerseWithHiddenWords(
    before: String,
    hidden: String,
    after: String,
    modifier: Modifier = Modifier,
    hiddenDescription: String = stringResource(Res.string.session_hidden_word),
) {
    val style = AppTextStyles.verse()
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(WORD_GAP),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        before.words().forEach { Text(it, style = style) }
        hidden.words().forEach { word ->
            Box(
                modifier = Modifier
                    .padding(vertical = Spacing.xs)
                    .width(MIST_WIDTH_PER_LETTER * word.length.coerceAtLeast(2))
                    .height(MIST_HEIGHT)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f), RoundedCornerShape(percent = 50))
                    .semantics { contentDescription = hiddenDescription },
            )
        }
        after.words().forEach { Text(it, style = style) }
    }
}

private fun String.words() = split(' ').filter { it.isNotBlank() }

private val WORD_GAP = 5.dp
private val MIST_WIDTH_PER_LETTER = 10.dp
private val MIST_HEIGHT = 20.dp
