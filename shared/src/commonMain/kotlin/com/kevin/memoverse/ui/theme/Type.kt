package com.kevin.memoverse.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.instrument_sans_400_normal
import com.kevin.memoverse.resources.instrument_sans_500_normal
import com.kevin.memoverse.resources.instrument_sans_600_normal
import com.kevin.memoverse.resources.instrument_serif_400_italic
import com.kevin.memoverse.resources.instrument_serif_400_normal
import com.kevin.memoverse.resources.newsreader_400_italic
import com.kevin.memoverse.resources.newsreader_400_normal
import com.kevin.memoverse.resources.newsreader_500_normal
import org.jetbrains.compose.resources.Font

/** Les trois familles de la direction « Jardin vivant ». */
@Immutable
data class AppFonts(
    /** Instrument Serif : titres. */
    val display: FontFamily = FontFamily.Serif,
    /** Newsreader : texte des versets et références. */
    val verse: FontFamily = FontFamily.Serif,
    /** Instrument Sans : interface. */
    val ui: FontFamily = FontFamily.SansSerif,
)

val LocalAppFonts = staticCompositionLocalOf { AppFonts() }

@Composable
internal fun rememberAppFonts(): AppFonts = AppFonts(
    display = FontFamily(
        Font(Res.font.instrument_serif_400_normal, FontWeight.Normal),
        Font(Res.font.instrument_serif_400_italic, FontWeight.Normal, FontStyle.Italic),
    ),
    verse = FontFamily(
        Font(Res.font.newsreader_400_normal, FontWeight.Normal),
        Font(Res.font.newsreader_500_normal, FontWeight.Medium),
        Font(Res.font.newsreader_400_italic, FontWeight.Normal, FontStyle.Italic),
    ),
    ui = FontFamily(
        Font(Res.font.instrument_sans_400_normal, FontWeight.Normal),
        Font(Res.font.instrument_sans_500_normal, FontWeight.Medium),
        Font(Res.font.instrument_sans_600_normal, FontWeight.SemiBold),
    ),
)

internal fun appTypography(fonts: AppFonts): Typography {
    val display = TextStyle(fontFamily = fonts.display, fontWeight = FontWeight.Normal, letterSpacing = (-0.01).em, color = Ink)
    val ui = TextStyle(fontFamily = fonts.ui, color = Ink)
    return Typography(
        displayLarge = display.copy(fontSize = 46.sp, lineHeight = 48.sp),
        displayMedium = display.copy(fontSize = 42.sp, lineHeight = 46.sp),
        headlineLarge = display.copy(fontSize = 40.sp, lineHeight = 44.sp),
        headlineMedium = display.copy(fontSize = 32.sp, lineHeight = 36.sp),
        headlineSmall = display.copy(fontSize = 28.sp, lineHeight = 32.sp),
        titleLarge = TextStyle(fontFamily = fonts.verse, fontSize = 18.sp, lineHeight = 24.sp, color = Ink),
        titleMedium = ui.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
        titleSmall = ui.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
        bodyLarge = ui.copy(fontSize = 16.sp, lineHeight = 22.sp),
        bodyMedium = ui.copy(fontSize = 15.sp, lineHeight = 21.sp),
        bodySmall = ui.copy(fontSize = 13.sp, lineHeight = 18.sp),
        labelLarge = ui.copy(fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
        labelMedium = ui.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.18.em),
        labelSmall = ui.copy(fontSize = 11.sp, lineHeight = 14.sp),
    )
}

/** Styles hors Material : texte de verset et logotype. */
object AppTextStyles {
    @Composable
    fun verse(): TextStyle = TextStyle(fontFamily = LocalAppFonts.current.verse, fontSize = 22.sp, lineHeight = 29.sp, color = Ink)

    @Composable
    fun verseQuote(): TextStyle = TextStyle(fontFamily = LocalAppFonts.current.verse, fontStyle = FontStyle.Italic, fontSize = 16.sp, lineHeight = 22.sp, color = Ink)

    @Composable
    fun wordmark(): TextStyle = TextStyle(fontFamily = LocalAppFonts.current.ui, fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 0.38.em)

    @Composable
    fun displayItalic(): TextStyle = TextStyle(fontFamily = LocalAppFonts.current.display, fontStyle = FontStyle.Italic, fontSize = 32.sp, lineHeight = 36.sp, color = Ink)
}
