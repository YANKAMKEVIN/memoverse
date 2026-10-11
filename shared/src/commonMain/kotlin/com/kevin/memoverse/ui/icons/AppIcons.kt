package com.kevin.memoverse.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Icônes en trait fin des maquettes « Jardin vivant » (grille 24, trait 1,8). */
object AppIcons {
    val Sprout: ImageVector by lazy {
        strokeIcon("Pousse") {
            moveTo(12f, 22f); lineTo(12f, 11f)
            moveTo(12f, 14f); curveTo(12f, 8f, 8f, 5f, 3f, 5f); curveTo(3f, 10f, 7f, 14f, 12f, 14f)
            moveTo(12f, 11f); curveTo(12f, 6f, 15f, 3f, 21f, 3f); curveTo(21f, 8f, 17f, 11f, 12f, 11f)
        }
    }
    val Explore: ImageVector by lazy {
        strokeIcon("Explorer") {
            for ((x, y) in listOf(4f to 4f, 14f to 4f, 4f to 14f, 14f to 14f)) {
                moveTo(x, y); lineTo(x + 6f, y); lineTo(x + 6f, y + 6f); lineTo(x, y + 6f); close()
            }
        }
    }
    val Progress: ImageVector by lazy {
        strokeIcon("Progression") {
            moveTo(5.5f, 20f); lineTo(5.5f, 13f)
            moveTo(12f, 20f); lineTo(12f, 8f)
            moveTo(18.5f, 20f); lineTo(18.5f, 4f)
        }
    }
    val Profile: ImageVector by lazy {
        strokeIcon("Profil") {
            moveTo(12f, 12f); curveTo(9.5f, 12f, 8f, 10f, 8f, 8f); curveTo(8f, 5.5f, 9.5f, 4f, 12f, 4f)
            curveTo(14.5f, 4f, 16f, 5.5f, 16f, 8f); curveTo(16f, 10f, 14.5f, 12f, 12f, 12f); close()
            moveTo(4f, 21f); curveTo(4f, 16.5f, 7.5f, 14.5f, 12f, 14.5f); curveTo(16.5f, 14.5f, 20f, 16.5f, 20f, 21f)
        }
    }
    val Check: ImageVector by lazy {
        strokeIcon("Coche", width = 2.2f) { moveTo(5f, 12.5f); lineTo(10f, 17f); lineTo(19f, 7f) }
    }
    val Microphone: ImageVector by lazy {
        strokeIcon("Micro", width = 2f) {
            moveTo(12f, 3f); curveTo(10.3f, 3f, 9f, 4.3f, 9f, 6f); lineTo(9f, 12f); curveTo(9f, 13.7f, 10.3f, 15f, 12f, 15f)
            curveTo(13.7f, 15f, 15f, 13.7f, 15f, 12f); lineTo(15f, 6f); curveTo(15f, 4.3f, 13.7f, 3f, 12f, 3f); close()
            moveTo(5.5f, 11.5f); curveTo(5.5f, 15.1f, 8.4f, 18f, 12f, 18f); curveTo(15.6f, 18f, 18.5f, 15.1f, 18.5f, 11.5f)
            moveTo(12f, 18f); lineTo(12f, 21f)
        }
    }
    val Stop: ImageVector by lazy {
        strokeIcon("Arrêter", width = 2f) { moveTo(8f, 8f); lineTo(16f, 8f); lineTo(16f, 16f); lineTo(8f, 16f); close() }
    }
    val Back: ImageVector by lazy {
        strokeIcon("Retour", width = 2f) { moveTo(15f, 5f); lineTo(8f, 12f); lineTo(15f, 19f) }
    }

    private fun strokeIcon(name: String, width: Float = 1.8f, block: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = width,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathBuilder = block,
            )
            .build()
}
