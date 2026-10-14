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
import com.kevin.memoverse.resources.figtree_400_normal
import com.kevin.memoverse.resources.figtree_500_normal
import com.kevin.memoverse.resources.figtree_600_normal
import com.kevin.memoverse.resources.fraunces_300_italic
import com.kevin.memoverse.resources.fraunces_300_normal
import com.kevin.memoverse.resources.fraunces_400_normal
import org.jetbrains.compose.resources.Font

/** Les trois familles de l'app : Fraunces (titres, versets) et Figtree (interface). */
@Immutable
data class AppFonts(
    /** Fraunces légère : titres. */
    val display: FontFamily = FontFamily.Serif,
    /** Fraunces : texte des versets et références. */
    val verse: FontFamily = FontFamily.Serif,
    /** Figtree : interface. */
    val ui: FontFamily = FontFamily.SansSerif,
)

val LocalAppFonts = staticCompositionLocalOf { AppFonts() }

@Composable
internal fun rememberAppFonts(): AppFonts = AppFonts(
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
)

internal fun appTypography(fonts: AppFonts): Typography {
    val display = TextStyle(fontFeatureSettings = LINING_FIGURES, fontFamily = fonts.display, fontWeight = FontWeight.Normal, letterSpacing = (-0.01).em, color = Ink)
    val d = DISPLAY_SCALE
    val v = VERSE_SCALE
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
    fun verse(): TextStyle = TextStyle(fontFeatureSettings = LINING_FIGURES, fontFamily = LocalAppFonts.current.verse, fontSize = (22 * VERSE_SCALE).sp, lineHeight = (29 * VERSE_SCALE).sp, color = Ink)

    @Composable
    fun verseQuote(): TextStyle = TextStyle(fontFeatureSettings = LINING_FIGURES, fontFamily = LocalAppFonts.current.verse, fontStyle = FontStyle.Italic, fontSize = (16 * VERSE_SCALE).sp, lineHeight = (22 * VERSE_SCALE).sp, color = Ink)

    @Composable
    fun wordmark(): TextStyle = TextStyle(fontFeatureSettings = LINING_FIGURES, fontFamily = LocalAppFonts.current.ui, fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 0.38.em)

    @Composable
    fun displayItalic(): TextStyle = TextStyle(fontFeatureSettings = LINING_FIGURES, fontFamily = LocalAppFonts.current.display, fontStyle = FontStyle.Italic, fontSize = (32 * DISPLAY_SCALE).sp, lineHeight = (36 * DISPLAY_SCALE).sp, color = Ink)
}

/** Fraunces a un œil plus grand que les tailles de la maquette : on la ramène à une taille perçue comparable. */
private const val DISPLAY_SCALE = 0.9f
private const val VERSE_SCALE = 0.94f

/** Chiffres alignés plutôt qu'« à l'ancienne », pour des références (3:16) bien lisibles. */
private const val LINING_FIGURES = "lnum"
