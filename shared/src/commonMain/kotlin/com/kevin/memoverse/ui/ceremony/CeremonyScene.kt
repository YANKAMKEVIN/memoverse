package com.kevin.memoverse.ui.ceremony

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import com.kevin.memoverse.presentation.common.StageUi
import com.kevin.memoverse.ui.theme.Cream
import com.kevin.memoverse.ui.theme.Gold
import com.kevin.memoverse.ui.theme.Leaf
import com.kevin.memoverse.ui.theme.Olive
import com.kevin.memoverse.ui.theme.Sage
import com.kevin.memoverse.ui.theme.SageLight
import com.kevin.memoverse.ui.theme.Sand
import com.kevin.memoverse.ui.theme.Terracotta
import com.kevin.memoverse.ui.theme.Water
import com.kevin.memoverse.ui.theme.WaterLight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Le « jardin de ton cœur » : une tranche de terre en coupe, dont la silhouette asymétrique dessine un cœur.
 * Sous la surface, la graine et ses racines ; au-dessus, la pousse, l'olivier et ses fruits.
 * Tout est vectoriel ; la géométrie est construite une fois par taille, seul le dessin suit l'horloge [time].
 */
@Composable
internal fun CeremonyScene(
    timeline: CeremonyTimeline,
    time: () -> Float,
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier.drawWithCache {
            val scene = SceneGeometry(size.width)
            onDrawBehind {
                val t = time()
                when (timeline) {
                    is PlantingTimeline -> drawPlanting(scene, timeline, t)
                    is WateringTimeline -> drawWatering(scene, timeline, t)
                    is WiltingTimeline -> drawWilting(scene, timeline, t)
                }
            }
        },
    )
}

/** Hauteur de la scène rapportée à sa largeur. */
internal const val SCENE_RATIO = 1.04f

// ---------------------------------------------------------------- Les trois scènes

private fun DrawScope.drawPlanting(scene: SceneGeometry, timeline: PlantingTimeline, t: Float) {
    val count = timeline.seedCount
    drawGround(scene, moisture = 0f, dryness = 0f) {
        // Les graines qui s'enfoncent passent sous la surface : la face du dessus les recouvre.
        repeat(count) { i ->
            val sink = timeline.sink(i).progress(t)
            if (sink > 0f) {
                val x = scene.seedX(i, count)
                val y = lerp(scene.surfaceY(x), scene.seedRestY(i), FastOutSlowInEasing.transform(sink))
                drawSeed(scene, Offset(x, y), scene.seedAngle(i))
            }
        }
    }
    val glow = timeline.glow.progress(t)
    if (glow > 0f) drawHalo(Offset(scene.u * 0.5f, scene.u * 0.6f), scene.u * 0.24f, sin(PI.toFloat() * glow))
    repeat(count) { i ->
        val x = scene.seedX(i, count)
        val ripple = timeline.ripple(i).progress(t)
        if (ripple in 0.001f..0.999f) drawRipple(scene, Offset(x, scene.surfaceY(x)), ripple)
        val fall = timeline.fall(i).progress(t)
        if (fall > 0f && timeline.sink(i).progress(t) == 0f) {
            // Chute accélérée, sans rebond ; une traînée dorée la suit.
            val top = scene.u * 0.02f
            val y = lerp(top, scene.surfaceY(x), fall * fall)
            val trail = scene.u * 0.13f * (1f - fall * 0.4f)
            drawLine(
                brush = Brush.verticalGradient(listOf(Gold.copy(alpha = 0f), Gold.copy(alpha = 0.75f)), startY = y - trail, endY = y),
                start = Offset(x, y - trail),
                end = Offset(x, y - scene.u * 0.02f),
                strokeWidth = scene.u * 0.007f,
                cap = StrokeCap.Round,
            )
            drawSeed(scene, Offset(x, y), scene.seedAngle(i) * fall)
        }
    }
}

private fun DrawScope.drawWatering(scene: SceneGeometry, timeline: WateringTimeline, t: Float) {
    val growth = timeline.growth(t)
    val moisture = LinearOutSlowInEasing.transform(timeline.moisture.progress(t))
    drawGround(scene, moisture = moisture, dryness = 0f) {
        drawUnderground(scene, growth, alpha = 1f)
    }
    drawAboveGround(scene, growth, alpha = 1f, droop = 0f)
    val halo = timeline.halo.progress(t)
    if (halo > 0f) {
        // Sur la graine tant qu'elle n'a pas germé, sur la couronne de l'arbre ensuite.
        val center = if (timeline.to == StageUi.SEED) scene.seedCenter else scene.crownCenter(timeline.to.level)
        drawHalo(center, scene.u * if (timeline.hasGrown) 0.3f else 0.2f, sin(PI.toFloat() * halo))
    }
    drawJugAndWater(scene, timeline, t)
}

private fun DrawScope.drawWilting(scene: SceneGeometry, timeline: WiltingTimeline, t: Float) {
    val dryness = FastOutSlowInEasing.transform(timeline.dry.progress(t))
    val droop = FastOutSlowInEasing.transform(timeline.droop.progress(t))
    val fade = FastOutSlowInEasing.transform(timeline.crossfade.progress(t))
    val from = timeline.from.level
    val to = timeline.to.level
    drawGround(scene, moisture = 1f - dryness, dryness = dryness) {
        // Les racines ne se rétractent pas brusquement : l'ancien réseau s'efface en fondu.
        drawUnderground(scene, from, alpha = 1f - fade * 0.6f)
        if (fade > 0f) drawUnderground(scene, to, alpha = fade)
    }
    drawAboveGround(scene, from, alpha = 1f - fade, droop = droop)
    if (fade > 0f) drawAboveGround(scene, to, alpha = fade, droop = droop)
}

// ---------------------------------------------------------------- Le cœur de terre

/** Ombre, coupe de terre, contenu souterrain ([underground]), face supérieure et décor. */
private fun DrawScope.drawGround(
    scene: SceneGeometry,
    moisture: Float,
    dryness: Float,
    underground: DrawScope.() -> Unit,
) {
    val u = scene.u
    // Ombre chaude sous l'île flottante.
    drawOval(
        brush = Brush.radialGradient(listOf(Earth.copy(alpha = 0.22f), Earth.copy(alpha = 0f)), center = Offset(u * 0.5f, u * 1.0f), radius = u * 0.28f),
        topLeft = Offset(u * 0.22f, u * 0.975f),
        size = Size(u * 0.56f, u * 0.05f),
    )
    drawPath(scene.heart, Brush.verticalGradient(listOf(EarthLight, Earth, EarthDeep), startY = u * 0.4f, endY = u * 0.98f))
    clipPath(scene.heart) {
        // Strates et cailloux : une coupe de terrain plutôt qu'un pictogramme.
        drawPath(scene.strata, EarthDeep.copy(alpha = 0.22f), style = Stroke(width = u * 0.005f, cap = StrokeCap.Round))
        scene.pebbles.forEach { (center, radius) -> drawCircle(EarthLight.copy(alpha = 0.35f), radius, center) }
        if (moisture > 0f) {
            drawCircle(
                brush = Brush.radialGradient(listOf(Moist.copy(alpha = 0.42f), Moist.copy(alpha = 0f)), center = scene.plantBase, radius = u * (0.05f + 0.38f * moisture)),
                radius = u * (0.05f + 0.38f * moisture),
                center = scene.plantBase,
            )
        }
        if (dryness > 0f) drawRect(Sand.copy(alpha = 0.3f * dryness))
        underground()
        clipPath(scene.topRegion) {
            drawRect(Brush.verticalGradient(listOf(TopSoilLight, TopSoil), startY = u * 0.34f, endY = u * 0.53f))
            if (moisture > 0f) {
                drawOval(
                    color = Moist.copy(alpha = 0.32f * moisture),
                    topLeft = Offset(u * (0.5f - 0.2f * moisture), u * (0.52f - 0.05f * moisture)),
                    size = Size(u * 0.4f * moisture, u * 0.1f * moisture),
                )
            }
            if (dryness > 0f) drawRect(Sand.copy(alpha = 0.35f * dryness))
        }
        drawPath(scene.surface, Cream.copy(alpha = 0.55f), style = Stroke(width = u * 0.006f, cap = StrokeCap.Round))
    }
    drawDecor(scene)
}

/** Deux cailloux, une touffe de sauge et un brin de lavande : le cœur appartient au jardin méditerranéen. */
private fun DrawScope.drawDecor(scene: SceneGeometry) {
    val u = scene.u
    drawOval(Cream, topLeft = Offset(u * 0.315f, u * 0.447f), size = Size(u * 0.04f, u * 0.02f))
    drawOval(SageLight, topLeft = Offset(u * 0.645f, u * 0.462f), size = Size(u * 0.03f, u * 0.016f))
    // Lavande sur l'épaule droite.
    val base = Offset(u * 0.775f, u * 0.405f)
    LAVENDER_STEMS.forEach { (dx, dy) ->
        val tip = Offset(base.x + u * dx, base.y + u * dy)
        drawLine(Sage, base, tip, strokeWidth = u * 0.005f, cap = StrokeCap.Round)
        repeat(3) { k ->
            val f = 1f - k * 0.16f
            val bud = Offset(base.x + u * dx * f, base.y + u * dy * f)
            drawOval(Lavender, topLeft = Offset(bud.x - u * 0.008f, bud.y - u * 0.012f), size = Size(u * 0.016f, u * 0.024f))
        }
    }
    // Touffe de sauge sur l'épaule gauche.
    val tuft = Offset(u * 0.235f, u * 0.418f)
    TUFT_ANGLES.forEach { angle ->
        rotate(angle, pivot = tuft) {
            drawOval(Sage, topLeft = Offset(tuft.x - u * 0.009f, tuft.y - u * 0.05f), size = Size(u * 0.018f, u * 0.05f))
        }
    }
}

// ---------------------------------------------------------------- La plante, selon sa croissance (0 graine → 3 fruit)

/** Graine en coupe et racines : 3 dès le stade Racine, 2 de plus en devenant arbre. */
private fun DrawScope.drawUnderground(scene: SceneGeometry, growth: Float, alpha: Float) {
    val u = scene.u
    val width = u * (0.007f + 0.004f * ((growth - 1f) / 2f).coerceIn(0f, 1f))
    scene.roots.forEachIndexed { i, root ->
        val start = if (i < FIRST_ROOTS) 0.1f else 1.1f
        val reveal = ((growth - start) / 0.8f).coerceIn(0f, 1f)
        if (reveal > 0f) drawRevealed(scene, root, reveal, RootColor.copy(alpha = 0.9f * alpha), width)
    }
    drawSeed(scene, scene.seedCenter, -15f, alpha)
}

private fun DrawScope.drawAboveGround(scene: SceneGeometry, growth: Float, alpha: Float, droop: Float) {
    if (alpha <= 0f || growth <= 0.2f) return
    val u = scene.u
    // Pousse : tige puis deux feuilles qui s'ouvrent ; elle s'efface quand le tronc prend le relais.
    val sproutAlpha = alpha * (1f - ((growth - 1.15f) / 0.35f).coerceIn(0f, 1f))
    if (sproutAlpha > 0f) {
        val stem = ((growth - 0.25f) / 0.45f).coerceIn(0f, 1f)
        drawRevealed(scene, scene.sprout, stem, Leaf.copy(alpha = sproutAlpha), u * 0.01f)
        val open = FastOutSlowInEasing.transform(((growth - 0.55f) / 0.45f).coerceIn(0f, 1f))
        if (open > 0f) {
            val tip = scene.sproutTip
            // Quand le verset fane, les feuilles s'inclinent d'une douzaine de degrés vers le sol.
            drawLeaf(tip, u * 0.1f * open, u * 0.042f * open, -30f - 30f * open - LEAF_DROOP_DEGREES * droop, lerp(Sage, Dull, 0.4f * droop).copy(alpha = sproutAlpha))
            drawLeaf(tip, u * 0.095f * open, u * 0.04f * open, 28f + 28f * open + LEAF_DROOP_DEGREES * droop, lerp(Leaf, Dull, 0.4f * droop).copy(alpha = sproutAlpha))
        }
    }
    if (growth <= 1f) return
    // Tronc dévoilé depuis le sol, puis les branches : jamais un arbre entier qui gonfle.
    drawRevealed(scene, scene.trunk, ((growth - 1f) / 0.45f).coerceIn(0f, 1f), Trunk.copy(alpha = alpha), u * 0.024f)
    scene.branches.forEachIndexed { i, branch ->
        val reveal = ((growth - 1.25f - i * 0.05f) / 0.35f).coerceIn(0f, 1f)
        if (reveal > 0f) drawRevealed(scene, branch, reveal, Trunk.copy(alpha = alpha), u * 0.011f)
    }
    val sag = u * 0.014f * droop
    FOLIAGE.forEach { cluster ->
        val p = FastOutSlowInEasing.transform(((growth - 1.45f - cluster.delay) / 0.35f).coerceIn(0f, 1f))
        if (p <= 0f) return@forEach
        val center = Offset(u * cluster.x, u * cluster.y + sag)
        val color = lerp(cluster.color, Dull, 0.45f * droop).copy(alpha = alpha * (0.6f + 0.35f * p))
        drawOval(color, topLeft = Offset(center.x - u * cluster.rx * p, center.y - u * cluster.ry * p), size = Size(u * cluster.rx * 2f * p, u * cluster.ry * 2f * p))
        // Reflet argenté de l'olivier.
        drawOval(
            SageLight.copy(alpha = alpha * 0.35f * p * (1f - droop * 0.6f)),
            topLeft = Offset(center.x - u * cluster.rx * 0.6f * p, center.y - u * cluster.ry * 0.75f * p),
            size = Size(u * cluster.rx * 0.8f * p, u * cluster.ry * 0.6f * p),
        )
    }
    if (growth <= 2f) return
    // Petites olives à reflet doré ; mates quand le verset fane.
    FRUITS.forEachIndexed { i, (x, y) ->
        val p = FastOutSlowInEasing.transform(((growth - 2.05f - i * 0.08f) / 0.4f).coerceIn(0f, 1f))
        if (p <= 0f) return@forEachIndexed
        val center = Offset(u * x, u * y + sag)
        val size = Size(u * 0.022f * p, u * 0.03f * p)
        val topLeft = Offset(center.x - size.width / 2f, center.y - size.height / 2f)
        drawOval(lerp(Gold, DullGold, droop).copy(alpha = alpha), topLeft, size)
        drawOval(GoldDeep.copy(alpha = alpha), topLeft, size, style = Stroke(width = u * 0.003f))
        drawCircle(Cream.copy(alpha = alpha * 0.7f * (1f - droop)), radius = size.width * 0.18f, center = Offset(center.x - size.width * 0.18f, center.y - size.height * 0.2f))
    }
}

/** Graine en forme d'amande, sans visage. */
private fun DrawScope.drawSeed(scene: SceneGeometry, center: Offset, angle: Float, alpha: Float = 1f) {
    val h = scene.u * 0.09f
    val w = scene.u * 0.06f
    val path = scene.scratch.apply {
        reset()
        moveTo(center.x, center.y - h / 2f)
        cubicTo(center.x + w * 0.78f, center.y - h * 0.25f, center.x + w * 0.58f, center.y + h * 0.5f, center.x, center.y + h / 2f)
        cubicTo(center.x - w * 0.58f, center.y + h * 0.5f, center.x - w * 0.78f, center.y - h * 0.25f, center.x, center.y - h / 2f)
        close()
    }
    rotate(angle, pivot = center) {
        drawPath(path, Brush.linearGradient(listOf(SeedLight.copy(alpha = alpha), SeedDark.copy(alpha = alpha)), start = Offset(center.x - w / 2f, center.y - h / 2f), end = Offset(center.x + w / 2f, center.y + h / 2f)))
        drawLine(SeedDark.copy(alpha = 0.5f * alpha), Offset(center.x, center.y - h * 0.32f), Offset(center.x, center.y + h * 0.32f), strokeWidth = scene.u * 0.003f, cap = StrokeCap.Round)
    }
}

/** Feuille en amande, attachée en [attach] et inclinée de [angle] degrés depuis la verticale. */
private fun DrawScope.drawLeaf(attach: Offset, length: Float, width: Float, angle: Float, color: Color) {
    if (length <= 0f) return
    rotate(angle, pivot = attach) {
        drawOval(color, topLeft = Offset(attach.x - width / 2f, attach.y - length), size = Size(width, length))
    }
}

/** Trace la part [fraction] d'un chemin depuis son début : racines, tige et tronc se dévoilent sans se déformer. */
private fun DrawScope.drawRevealed(scene: SceneGeometry, path: MeasuredPath, fraction: Float, color: Color, width: Float) {
    if (fraction >= 1f) {
        drawPath(path.path, color, style = Stroke(width = width, cap = StrokeCap.Round))
        return
    }
    val segment = scene.scratch.apply { reset() }
    path.measure.getSegment(0f, path.length * fraction, segment, true)
    drawPath(segment, color, style = Stroke(width = width, cap = StrokeCap.Round))
}

private fun DrawScope.drawHalo(center: Offset, radius: Float, intensity: Float) {
    if (intensity <= 0f) return
    drawCircle(Brush.radialGradient(listOf(Gold.copy(alpha = 0.5f * intensity), Gold.copy(alpha = 0f)), center = center, radius = radius), radius = radius, center = center)
}

/** Onde de contact qui s'élargit en ralentissant. */
private fun DrawScope.drawRipple(scene: SceneGeometry, center: Offset, progress: Float) {
    val eased = LinearOutSlowInEasing.transform(progress)
    val width = scene.u * (0.04f + 0.16f * eased)
    drawOval(
        color = Cream.copy(alpha = 0.85f * (1f - progress)),
        topLeft = Offset(center.x - width / 2f, center.y - width * 0.16f),
        size = Size(width, width * 0.32f),
        style = Stroke(width = scene.u * 0.004f),
    )
}

// ---------------------------------------------------------------- La cruche et l'eau

private fun DrawScope.drawJugAndWater(scene: SceneGeometry, timeline: WateringTimeline, t: Float) {
    val u = scene.u
    val appear = FastOutSlowInEasing.transform(timeline.jugIn.progress(t)) * (1f - timeline.jugOut.progress(t))
    if (appear <= 0f) return
    val tilt = FastOutSlowInEasing.transform(timeline.tilt.progress(t)) * (1f - FastOutSlowInEasing.transform(timeline.tiltBack.progress(t)))
    val angle = JUG_TILT_DEGREES * tilt
    val pivot = Offset(u * (JUG_X + 0.08f * (1f - appear)), u * JUG_Y)
    withTransform({
        translate(pivot.x, pivot.y)
        rotate(angle, pivot = Offset.Zero)
        scale(JUG_SCALE, JUG_SCALE, pivot = Offset.Zero)
    }) {
        drawPath(scene.jugHandle, TerracottaDeep.copy(alpha = appear), style = Stroke(width = u * 0.01f, cap = StrokeCap.Round))
        drawPath(scene.jugSpout, Terracotta.copy(alpha = appear))
        drawPath(scene.jugBody, Brush.horizontalGradient(listOf(TerracottaLight.copy(alpha = appear), Terracotta.copy(alpha = appear), TerracottaDeep.copy(alpha = appear)), startX = -u * 0.07f, endX = u * 0.07f))
        drawRect(TerracottaDeep.copy(alpha = appear), topLeft = Offset(-u * 0.03f, -u * 0.084f), size = Size(u * 0.06f, u * 0.012f))
        drawLine(Cream.copy(alpha = 0.35f * appear), Offset(-u * 0.055f, -u * 0.005f), Offset(u * 0.055f, -u * 0.005f), strokeWidth = u * 0.004f)
    }
    // Les gouttes partent du bec, qui tourne avec la cruche.
    val radians = angle * PI.toFloat() / 180f
    val tip = Offset(
        pivot.x + u * JUG_SCALE * (SPOUT_TIP_X * cos(radians) - SPOUT_TIP_Y * sin(radians)),
        pivot.y + u * JUG_SCALE * (SPOUT_TIP_X * sin(radians) + SPOUT_TIP_Y * cos(radians)),
    )
    val target = scene.plantBase
    val pour = timeline.pour
    var emitted = pour.start
    var k = 0
    while (emitted < pour.end && k < MAX_DROPS) {
        val p = (t - emitted) / DROP_FALL_MILLIS
        if (p in 0f..1f) {
            val jitter = u * 0.006f * ((k * 37) % 7 - 3)
            val center = Offset(lerp(tip.x, target.x + jitter, p), tip.y + (target.y - tip.y) * p * p)
            val r = u * 0.011f
            drawOval(Water, topLeft = Offset(center.x - r, center.y - r * (1f + 0.6f * p)), size = Size(r * 2f, r * 2f * (1f + 0.3f * p)))
            drawCircle(WaterLight, radius = r * 0.35f, center = Offset(center.x - r * 0.3f, center.y - r * 0.4f))
        }
        emitted += DROP_INTERVAL_MILLIS
        k++
    }
    // Petites ondes là où l'eau touche la terre.
    val splashTime = t - pour.start - DROP_FALL_MILLIS
    if (splashTime > 0f && t < pour.end + DROP_FALL_MILLIS) {
        drawRipple(scene, target, (splashTime % SPLASH_MILLIS) / SPLASH_MILLIS)
    }
}

// ---------------------------------------------------------------- Géométrie, construite une fois par taille

private class MeasuredPath(val path: Path) {
    val measure = PathMeasure().apply { setPath(path, false) }
    val length = measure.length
}

private class SceneGeometry(val u: Float) {
    private fun p(x: Float, y: Float) = Offset(x * u, y * u)

    /** Chemin réutilisé à chaque image, pour ne rien allouer pendant l'animation. */
    val scratch = Path()

    val heart = Path().apply {
        moveTo(0.5f * u, 0.44f * u)
        cubicTo(0.47f * u, 0.37f * u, 0.40f * u, 0.34f * u, 0.31f * u, 0.345f * u)
        cubicTo(0.18f * u, 0.35f * u, 0.11f * u, 0.43f * u, 0.12f * u, 0.53f * u)
        cubicTo(0.13f * u, 0.66f * u, 0.30f * u, 0.80f * u, 0.47f * u, 0.955f * u)
        quadraticTo(0.5f * u, 0.985f * u, 0.53f * u, 0.955f * u)
        cubicTo(0.70f * u, 0.80f * u, 0.875f * u, 0.67f * u, 0.885f * u, 0.545f * u)
        cubicTo(0.895f * u, 0.44f * u, 0.82f * u, 0.365f * u, 0.70f * u, 0.36f * u)
        cubicTo(0.61f * u, 0.355f * u, 0.535f * u, 0.385f * u, 0.5f * u, 0.44f * u)
        close()
    }

    /** Ce qui est au-dessus de la surface : la face du dessus du lit de terre, vue légèrement d'en haut. */
    val topRegion = Path().apply {
        moveTo(0f, 0f)
        lineTo(u, 0f)
        lineTo(u, SURFACE_EDGE * u)
        quadraticTo(0.5f * u, SURFACE_DIP * u, 0f, SURFACE_EDGE * u)
        close()
    }

    val surface = Path().apply {
        moveTo(0.1f * u, surfaceAt(0.1f) * u)
        quadraticTo(0.5f * u, SURFACE_DIP * u, 0.9f * u, surfaceAt(0.9f) * u)
    }

    val strata = Path().apply {
        moveTo(0.12f * u, 0.66f * u)
        quadraticTo(0.5f * u, 0.71f * u, 0.88f * u, 0.64f * u)
        moveTo(0.22f * u, 0.8f * u)
        quadraticTo(0.5f * u, 0.84f * u, 0.78f * u, 0.79f * u)
    }

    val pebbles = listOf(p(0.29f, 0.72f) to 0.014f * u, p(0.7f, 0.69f) to 0.011f * u, p(0.57f, 0.89f) to 0.009f * u, p(0.38f, 0.6f) to 0.008f * u)

    val plantBase = p(0.5f, surfaceAt(0.5f))
    val seedCenter = p(0.5f, 0.6f)

    val roots = listOf(
        root(0.5f, 0.62f, 0.49f, 0.70f, 0.52f, 0.78f, 0.505f, 0.87f),
        root(0.5f, 0.62f, 0.46f, 0.66f, 0.39f, 0.67f, 0.31f, 0.735f),
        root(0.5f, 0.62f, 0.55f, 0.66f, 0.62f, 0.68f, 0.69f, 0.745f),
        root(0.5f, 0.63f, 0.47f, 0.71f, 0.42f, 0.76f, 0.385f, 0.845f),
        root(0.5f, 0.63f, 0.53f, 0.72f, 0.59f, 0.77f, 0.625f, 0.855f),
    )

    val sprout = MeasuredPath(Path().apply {
        moveTo(0.5f * u, 0.57f * u)
        cubicTo(0.505f * u, 0.52f * u, 0.488f * u, 0.45f * u, 0.5f * u, 0.38f * u)
    })
    val sproutTip = p(0.5f, 0.38f)

    val trunk = MeasuredPath(Path().apply {
        moveTo(0.5f * u, 0.535f * u)
        cubicTo(0.506f * u, 0.45f * u, 0.484f * u, 0.36f * u, 0.497f * u, 0.255f * u)
    })

    val branches = listOf(
        branch(0.494f, 0.37f, 0.45f, 0.33f, 0.4f, 0.29f),
        branch(0.497f, 0.33f, 0.55f, 0.3f, 0.6f, 0.255f),
        branch(0.497f, 0.29f, 0.48f, 0.24f, 0.465f, 0.2f),
        branch(0.497f, 0.3f, 0.53f, 0.24f, 0.55f, 0.195f),
    )

    /** Centre du feuillage visible au niveau [level] : là où le halo se pose. */
    fun crownCenter(level: Float): Offset = when {
        level >= 2f -> p(0.5f, 0.23f)
        else -> p(0.5f, 0.4f)
    }

    // Cruche en terre cuite, dessinée autour de son centre (pivot de l'inclinaison).
    val jugBody = Path().apply {
        moveTo(-0.035f * u, -0.045f * u)
        cubicTo(-0.078f * u, -0.03f * u, -0.078f * u, 0.05f * u, -0.02f * u, 0.06f * u)
        lineTo(0.02f * u, 0.06f * u)
        cubicTo(0.078f * u, 0.05f * u, 0.078f * u, -0.03f * u, 0.035f * u, -0.045f * u)
        lineTo(0.024f * u, -0.074f * u)
        lineTo(-0.024f * u, -0.074f * u)
        close()
    }
    val jugSpout = Path().apply {
        moveTo(-0.028f * u, -0.064f * u)
        lineTo(SPOUT_TIP_X * u, (SPOUT_TIP_Y - 0.004f) * u)
        lineTo((SPOUT_TIP_X + 0.006f) * u, (SPOUT_TIP_Y + 0.01f) * u)
        lineTo(-0.034f * u, -0.04f * u)
        close()
    }
    val jugHandle = Path().apply {
        moveTo(0.026f * u, -0.062f * u)
        cubicTo(0.09f * u, -0.068f * u, 0.09f * u, 0.02f * u, 0.058f * u, 0.026f * u)
    }

    fun seedX(index: Int, count: Int): Float = u * (0.5f + (index - (count - 1) / 2f) * SEED_SPACING)
    fun seedRestY(index: Int): Float = u * (0.585f + (index % 2) * 0.025f)
    fun seedAngle(index: Int): Float = if (index % 2 == 0) -18f else 16f

    /** Hauteur de la surface à l'abscisse [x] (en pixels). */
    fun surfaceY(x: Float): Float = surfaceAt(x / u) * u

    private fun root(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) =
        MeasuredPath(Path().apply {
            moveTo(x0 * u, y0 * u)
            cubicTo(x1 * u, y1 * u, x2 * u, y2 * u, x3 * u, y3 * u)
        })

    private fun branch(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float) =
        MeasuredPath(Path().apply {
            moveTo(x0 * u, y0 * u)
            quadraticTo(x1 * u, y1 * u, x2 * u, y2 * u)
        })

    private companion object {
        const val SURFACE_EDGE = 0.47f
        const val SURFACE_DIP = 0.57f
        const val SEED_SPACING = 0.085f

        /** Surface courbe entre (0,1 ; 0,47) et (0,9 ; 0,47), au plus bas (0,52) au centre. */
        fun surfaceAt(x: Float): Float {
            val s = ((x - 0.1f) / 0.8f).coerceIn(0f, 1f)
            return SURFACE_EDGE + 2f * (SURFACE_DIP - SURFACE_EDGE) * s * (1f - s)
        }
    }
}

private class Cluster(val x: Float, val y: Float, val rx: Float, val ry: Float, val color: Color, val delay: Float)

private val FOLIAGE = listOf(
    Cluster(0.385f, 0.275f, 0.075f, 0.05f, Olive, 0f),
    Cluster(0.5f, 0.245f, 0.07f, 0.05f, Sage, 0.04f),
    Cluster(0.455f, 0.205f, 0.08f, 0.055f, Leaf, 0.06f),
    Cluster(0.55f, 0.19f, 0.08f, 0.055f, Olive, 0.1f),
    Cluster(0.5f, 0.15f, 0.06f, 0.04f, Leaf, 0.13f),
    Cluster(0.615f, 0.245f, 0.07f, 0.05f, Leaf, 0.16f),
    Cluster(0.335f, 0.225f, 0.05f, 0.035f, Sage, 0.2f),
    Cluster(0.665f, 0.2f, 0.05f, 0.035f, Sage, 0.22f),
)

private val FRUITS = listOf(0.41f to 0.29f, 0.47f to 0.225f, 0.535f to 0.265f, 0.585f to 0.205f, 0.635f to 0.26f, 0.5f to 0.17f)
private val LAVENDER_STEMS = listOf(-0.022f to -0.07f, 0.004f to -0.085f, 0.026f to -0.065f)
private val TUFT_ANGLES = listOf(-40f, -15f, 10f, 35f)

private const val FIRST_ROOTS = 3
private const val LEAF_DROOP_DEGREES = 12f
private const val JUG_X = 0.76f
private const val JUG_Y = 0.15f
private const val JUG_SCALE = 1.5f
private const val JUG_TILT_DEGREES = -32f
private const val SPOUT_TIP_X = -0.095f
private const val SPOUT_TIP_Y = -0.08f
private const val MAX_DROPS = 9
private const val DROP_INTERVAL_MILLIS = 100f
private const val DROP_FALL_MILLIS = 420f
private const val SPLASH_MILLIS = 380f

private fun lerp(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction

// Teintes de terre, de graine et de cruche, dérivées de la palette (terre #8E6748).
private val EarthLight = Color(0xFFC39A70)
private val Earth = Color(0xFF8E6748)
private val EarthDeep = Color(0xFF6E4E35)
private val TopSoil = Color(0xFFB98D63)
private val TopSoilLight = Color(0xFFD3B089)
private val Moist = Color(0xFF4F3622)
private val RootColor = Color(0xFFF1E4CE)
private val SeedLight = Color(0xFFB0835A)
private val SeedDark = Color(0xFF6B4A30)
private val Trunk = Color(0xFF6B4F38)
private val Lavender = Color(0xFF9D8DB8)
private val TerracottaLight = Color(0xFFCB7C5E)
private val TerracottaDeep = Color(0xFF8E4430)
private val GoldDeep = Color(0xFFB8924A)
/** Feuillage qui fane : un vert éteint, tirant sur le sable. */
private val Dull = Color(0xFFA9A184)
private val DullGold = Color(0xFFCDBB94)
