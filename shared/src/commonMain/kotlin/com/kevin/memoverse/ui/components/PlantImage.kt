package com.kevin.memoverse.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.presentation.common.StageUi
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.plante_arbre
import com.kevin.memoverse.resources.plante_fruit
import com.kevin.memoverse.resources.plante_graine
import com.kevin.memoverse.resources.plante_racine
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * La plante d'un verset à son stade, détourée (miniatures tirées des illustrations stade_*).
 * Toutes partagent la même échelle : le socle a la même largeur, seule la plante grandit.
 * [scale] = dp par pixel de l'image ; une plante qui a soif penche et pâlit ; la nuit, elle prend la lumière bleutée de la scène.
 */
@Composable
fun PlantImage(
    stage: StageUi,
    modifier: Modifier = Modifier,
    scale: Float = DEFAULT_SCALE,
    isThirsty: Boolean = false,
    isNight: Boolean = false,
) {
    val art = stage.plantArt()
    val colorFilter = remember(isThirsty, isNight) { plantFilter(isThirsty, isNight) }
    Image(
        painter = painterResource(art.drawable),
        contentDescription = null,
        colorFilter = colorFilter,
        modifier = modifier
            .width((art.widthPx * scale).dp)
            .height((art.heightPx * scale).dp)
            .graphicsLayer {
                if (isThirsty) {
                    rotationZ = THIRSTY_TILT_DEGREES
                    transformOrigin = TransformOrigin(0.5f, 1f)
                    alpha = THIRSTY_ALPHA
                }
            },
    )
}

private class PlantArt(val drawable: DrawableResource, val widthPx: Int, val heightPx: Int)

private fun StageUi.plantArt(): PlantArt = when (this) {
    StageUi.SEED -> PlantArt(Res.drawable.plante_graine, 145, 88)
    StageUi.ROOT -> PlantArt(Res.drawable.plante_racine, 150, 154)
    StageUi.TREE -> PlantArt(Res.drawable.plante_arbre, 150, 256)
    StageUi.FRUIT -> PlantArt(Res.drawable.plante_fruit, 220, 320)
}

private const val DEFAULT_SCALE = 0.3f
private const val THIRSTY_TILT_DEGREES = -8f
private const val THIRSTY_ALPHA = 0.88f
private const val THIRSTY_SATURATION = 0.35f

private fun plantFilter(isThirsty: Boolean, isNight: Boolean): ColorFilter? {
    if (!isThirsty && !isNight) return null
    val matrix = ColorMatrix()
    if (isThirsty) matrix.setToSaturation(THIRSTY_SATURATION)
    if (isNight) matrix.timesAssign(ColorMatrix().apply { setToScale(NIGHT_RED, NIGHT_GREEN, NIGHT_BLUE, 1f) })
    return ColorFilter.colorMatrix(matrix)
}

private const val NIGHT_RED = 0.62f
private const val NIGHT_GREEN = 0.68f
private const val NIGHT_BLUE = 0.85f
