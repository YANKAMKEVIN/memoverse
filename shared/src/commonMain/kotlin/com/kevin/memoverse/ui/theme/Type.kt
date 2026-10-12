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
import com.kevin.memoverse.presentation.common.TypefaceUi
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.cormorant_garamond_500_italic
import com.kevin.memoverse.resources.cormorant_garamond_500_normal
import com.kevin.memoverse.resources.eb_garamond_400_italic
import com.kevin.memoverse.resources.eb_garamond_400_normal
import com.kevin.memoverse.resources.eb_garamond_500_normal
import com.kevin.memoverse.resources.figtree_400_normal
import com.kevin.memoverse.resources.figtree_500_normal
import com.kevin.memoverse.resources.figtree_600_normal
import com.kevin.memoverse.resources.fraunces_300_italic
import com.kevin.memoverse.resources.fraunces_300_normal
import com.kevin.memoverse.resources.fraunces_400_normal
import com.kevin.memoverse.resources.geist_400_normal
import com.kevin.memoverse.resources.geist_500_normal
import com.kevin.memoverse.resources.geist_600_normal
import com.kevin.memoverse.resources.gloock_400_normal
import com.kevin.memoverse.resources.manrope_400_normal
import com.kevin.memoverse.resources.manrope_500_normal
import com.kevin.memoverse.resources.manrope_600_normal
import com.kevin.memoverse.resources.source_serif_4_400_italic
import com.kevin.memoverse.resources.source_serif_4_400_normal
import com.kevin.memoverse.resources.source_serif_4_500_normal
import com.kevin.memoverse.resources.instrument_sans_400_normal
import com.kevin.memoverse.resources.instrument_sans_500_normal
import com.kevin.memoverse.resources.instrument_sans_600_normal
import com.kevin.memoverse.resources.instrument_serif_400_italic
import com.kevin.memoverse.resources.instrument_serif_400_normal
import com.kevin.memoverse.resources.newsreader_400_italic
import com.kevin.memoverse.resources.newsreader_400_normal
import com.kevin.memoverse.resources.newsreader_500_normal
import org.jetbrains.compose.resources.Font

/** Les trois familles d'une association de polices, avec leur correction de taille. */
@Immutable
data class AppFonts(
    /** Titres. */
    val display: FontFamily = FontFamily.Serif,
    /** Texte des versets et références. */
    val verse: FontFamily = FontFamily.Serif,
    /** Interface. */
    val ui: FontFamily = FontFamily.SansSerif,
    /** Les polices n'ont pas le même œil : on ramène chaque titre à une taille perçue comparable. */
    val displayScale: Float = 1f,
    val verseScale: Float = 1f,
)

val LocalAppFonts = staticCompositionLocalOf { AppFonts() }

@Composable
internal fun rememberAppFonts(typeface: TypefaceUi = TypefaceUi.JARDIN): AppFonts = when (typeface) {
    TypefaceUi.JARDIN -> AppFonts(
        display = FontFamily(
            Font(Res.font.instrument_serif_400_normal, FontWeight.Normal),
            Font(Res.font.instrument_serif_400_italic, FontWeight.Normal, FontStyle.Italic),
        ),
        verse = FontFamily(
            Font(Res.font.newsreader_400_normal, FontWeight.Normal),
            Font(Res.font.newsreader_500_normal, FontWeight.Medium),
            Font(Res.font.newsreader_400_italic, FontWeight.Normal, FontStyle.Italic),
        ),
        ui = instrumentSans(),
    )
    TypefaceUi.FRAUNCES -> AppFonts(
        display = FontFamily(
            Font(Res.font.fraunces_300_normal, FontWeight.Normal),
            Font(Res.font.fraunces_300_italic, FontWeight.Normal, FontStyle.Italic),
        ),
        verse = FontFamily(
            Font(Res.font.fraunces_400_normal, FontWeight.Normal),
            Font(Res.font.fraunces_400_normal, FontWeight.Medium),
            Font(Res.font.fraunces_300_italic, FontWeight.Normal, FontStyle.Italic),
        ),
        ui = FontFamily(
            Font(Res.font.figtree_400_normal, FontWeight.Normal),
            Font(Res.font.figtree_500_normal, FontWeight.Medium),
            Font(Res.font.figtree_600_normal, FontWeight.SemiBold),
        ),
        displayScale = 0.9f,
        verseScale = 0.94f,
    )
    TypefaceUi.CORMORANT -> AppFonts(
        display = FontFamily(
            Font(Res.font.cormorant_garamond_500_normal, FontWeight.Normal),
            Font(Res.font.cormorant_garamond_500_italic, FontWeight.Normal, FontStyle.Italic),
        ),
        verse = FontFamily(
            Font(Res.font.cormorant_garamond_500_normal, FontWeight.Normal),
            Font(Res.font.cormorant_garamond_500_normal, FontWeight.Medium),
            Font(Res.font.cormorant_garamond_500_italic, FontWeight.Normal, FontStyle.Italic),
        ),
        ui = FontFamily(
            Font(Res.font.manrope_400_normal, FontWeight.Normal),
            Font(Res.font.manrope_500_normal, FontWeight.Medium),
            Font(Res.font.manrope_600_normal, FontWeight.SemiBold),
        ),
        displayScale = 1.04f,
        verseScale = 1.1f,
    )
    TypefaceUi.GLOOCK -> AppFonts(
        display = FontFamily(
            Font(Res.font.gloock_400_normal, FontWeight.Normal),
            Font(Res.font.source_serif_4_400_italic, FontWeight.Normal, FontStyle.Italic),
        ),
        verse = FontFamily(
            Font(Res.font.source_serif_4_400_normal, FontWeight.Normal),
            Font(Res.font.source_serif_4_500_normal, FontWeight.Medium),
            Font(Res.font.source_serif_4_400_italic, FontWeight.Normal, FontStyle.Italic),
        ),
        ui = geist(),
        displayScale = 0.84f,
        verseScale = 0.92f,
    )
    TypefaceUi.GARAMOND -> AppFonts(
        display = FontFamily(
            Font(Res.font.eb_garamond_400_normal, FontWeight.Normal),
            Font(Res.font.eb_garamond_400_italic, FontWeight.Normal, FontStyle.Italic),
        ),
        verse = FontFamily(
            Font(Res.font.eb_garamond_400_normal, FontWeight.Normal),
            Font(Res.font.eb_garamond_500_normal, FontWeight.Medium),
            Font(Res.font.eb_garamond_400_italic, FontWeight.Normal, FontStyle.Italic),
        ),
        ui = geist(),
        displayScale = 0.95f,
        verseScale = 1.04f,
    )
}

@Composable
private fun instrumentSans() = FontFamily(
    Font(Res.font.instrument_sans_400_normal, FontWeight.Normal),
    Font(Res.font.instrument_sans_500_normal, FontWeight.Medium),
    Font(Res.font.instrument_sans_600_normal, FontWeight.SemiBold),
)

@Composable
private fun geist() = FontFamily(
    Font(Res.font.geist_400_normal, FontWeight.Normal),
    Font(Res.font.geist_500_normal, FontWeight.Medium),
    Font(Res.font.geist_600_normal, FontWeight.SemiBold),
)

internal fun appTypography(fonts: AppFonts): Typography {
    val display = TextStyle(fontFeatureSettings = LINING_FIGURES, fontFamily = fonts.display, fontWeight = FontWeight.Normal, letterSpacing = (-0.01).em, color = Ink)
    val d = fonts.displayScale
    val v = fonts.verseScale
    val ui = TextStyle(fontFeatureSettings = LINING_FIGURES, fontFamily = fonts.ui, color = Ink)
    return Typography(
        displayLarge = display.copy(fontSize = (46 * d).sp, lineHeight = (48 * d).sp),
        displayMedium = display.copy(fontSize = (42 * d).sp, lineHeight = (46 * d).sp),
        headlineLarge = display.copy(fontSize = (40 * d).sp, lineHeight = (44 * d).sp),
        headlineMedium = display.copy(fontSize = (32 * d).sp, lineHeight = (36 * d).sp),
        headlineSmall = display.copy(fontSize = (28 * d).sp, lineHeight = (32 * d).sp),
        titleLarge = TextStyle(fontFeatureSettings = LINING_FIGURES, fontFamily = fonts.verse, fontSize = (18 * v).sp, lineHeight = (24 * v).sp, color = Ink),
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
    fun verse(): TextStyle = LocalAppFonts.current.let { TextStyle(fontFeatureSettings = LINING_FIGURES, fontFamily = it.verse, fontSize = (22 * it.verseScale).sp, lineHeight = (29 * it.verseScale).sp, color = Ink) }

    @Composable
    fun verseQuote(): TextStyle = LocalAppFonts.current.let { TextStyle(fontFeatureSettings = LINING_FIGURES, fontFamily = it.verse, fontStyle = FontStyle.Italic, fontSize = (16 * it.verseScale).sp, lineHeight = (22 * it.verseScale).sp, color = Ink) }

    @Composable
    fun wordmark(): TextStyle = TextStyle(fontFeatureSettings = LINING_FIGURES, fontFamily = LocalAppFonts.current.ui, fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 0.38.em)

    @Composable
    fun displayItalic(): TextStyle = LocalAppFonts.current.let { TextStyle(fontFeatureSettings = LINING_FIGURES, fontFamily = it.display, fontStyle = FontStyle.Italic, fontSize = (32 * it.displayScale).sp, lineHeight = (36 * it.displayScale).sp, color = Ink) }
}

/** Chiffres alignés : Cormorant et Garamond dessinent sinon des chiffres « à l'ancienne » (« 1 » lu « I »). */
internal const val LINING_FIGURES = "lnum"
