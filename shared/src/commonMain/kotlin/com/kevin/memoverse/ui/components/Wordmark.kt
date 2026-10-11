package com.kevin.memoverse.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.app_wordmark
import com.kevin.memoverse.ui.theme.AppTextStyles
import com.kevin.memoverse.ui.theme.Ink
import org.jetbrains.compose.resources.stringResource

@Composable
fun Wordmark(
    modifier: Modifier = Modifier,
    color: Color = Ink,
) {
    Text(
        text = stringResource(Res.string.app_wordmark),
        style = AppTextStyles.wordmark(),
        color = color.copy(alpha = 0.85f),
        modifier = modifier,
    )
}
