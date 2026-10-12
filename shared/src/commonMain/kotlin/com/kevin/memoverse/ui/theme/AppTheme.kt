package com.kevin.memoverse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.kevin.memoverse.presentation.common.TypefaceUi

private val JardinColors = lightColorScheme(
    primary = Olive,
    onPrimary = OliveText,
    primaryContainer = SageLight,
    onPrimaryContainer = Olive,
    secondary = Sage,
    onSecondary = OliveText,
    background = Sand,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = IllustrationBackground,
    onSurfaceVariant = InkMuted,
    outline = Hairline,
    error = Terracotta,
)

@Composable
fun AppTheme(typeface: TypefaceUi = TypefaceUi.JARDIN, content: @Composable () -> Unit) {
    val fonts = rememberAppFonts(typeface)
    CompositionLocalProvider(LocalAppFonts provides fonts) {
        MaterialTheme(colorScheme = JardinColors, typography = appTypography(fonts), content = content)
    }
}
