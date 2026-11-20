package com.kevin.memoverse.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.ile_mission
import com.kevin.memoverse.resources.ile_moments_de_vie
import com.kevin.memoverse.resources.ile_promesses_de_dieu
import com.kevin.memoverse.resources.ile_relation_avec_dieu
import com.kevin.memoverse.resources.ile_relations
import com.kevin.memoverse.resources.ile_vie_chretienne
import com.kevin.memoverse.resources.ile_vie_interieure
import org.jetbrains.compose.resources.DrawableResource

/** Chaque famille de thèmes a son île ; le nom vient de la banque de versets. */
fun familyIsland(familyName: String): DrawableResource = when (familyName) {
    "Relation avec Dieu" -> Res.drawable.ile_relation_avec_dieu
    "Promesses de Dieu" -> Res.drawable.ile_promesses_de_dieu
    "Vie intérieure" -> Res.drawable.ile_vie_interieure
    "Vie chrétienne" -> Res.drawable.ile_vie_chretienne
    "Relations" -> Res.drawable.ile_relations
    "Moments de vie" -> Res.drawable.ile_moments_de_vie
    else -> Res.drawable.ile_mission
}

/** Fond les bords de l'illustration dans le ciel (son fond crème ne se voit plus). */
fun Modifier.softEdges(): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.radialGradient(0.62f to Color.Black, 1f to Color.Transparent),
            blendMode = BlendMode.DstIn,
        )
    }
