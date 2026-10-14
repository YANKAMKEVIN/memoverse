package com.kevin.memoverse.ui.ceremony

import androidx.compose.animation.core.FastOutSlowInEasing
import com.kevin.memoverse.presentation.ceremony.CeremonyUi
import com.kevin.memoverse.presentation.common.StageUi

/** Fenêtre de temps d'une étape, en millisecondes depuis le début de la scène. */
internal data class Phase(val start: Float, val end: Float) {
    /** Avancement dans l'étape, de 0 (pas commencée) à 1 (terminée). */
    fun progress(t: Float): Float = when {
        t <= start -> 0f
        t >= end -> 1f
        else -> (t - start) / (end - start)
    }
}

/**
 * Chronologie d'une scène : une seule horloge (le temps écoulé) dont chaque étape déduit ses paramètres.
 * La durée totale reste entre 3 et 5 s pour la plantation et l'arrosage ; la variante « fane » est plus brève.
 */
internal sealed interface CeremonyTimeline {
    val totalMillis: Int
    val fadeIn: Phase get() = Phase(0f, FADE_IN_MILLIS)

    /** Le libellé du stade apparaît à la fin, une fois la plante à sa taille. */
    val label: Phase

    companion object {
        fun of(ceremony: CeremonyUi): CeremonyTimeline = when (ceremony) {
            is CeremonyUi.Planting -> PlantingTimeline(ceremony.references.size.coerceIn(1, MAX_SEEDS))
            is CeremonyUi.Watering -> WateringTimeline(ceremony.from, ceremony.to)
            is CeremonyUi.Wilting -> WiltingTimeline(ceremony.from, ceremony.to)
        }
    }
}

/** Les graines tombent l'une après l'autre, touchent la terre, s'y enfoncent ; une lueur dorée les accueille. */
internal class PlantingTimeline(val seedCount: Int) : CeremonyTimeline {
    fun fall(seed: Int): Phase {
        val start = FALL_START + seed * SEED_STAGGER
        return Phase(start, start + FALL_MILLIS)
    }

    fun ripple(seed: Int): Phase = fall(seed).end.let { Phase(it, it + RIPPLE_MILLIS) }

    fun sink(seed: Int): Phase = fall(seed).end.let { Phase(it + SINK_DELAY, it + SINK_DELAY + SINK_MILLIS) }

    val glow: Phase = sink(seedCount - 1).end.let { Phase(it - GLOW_OVERLAP, it - GLOW_OVERLAP + GLOW_MILLIS) }
    override val label: Phase = Phase(glow.start, glow.start + LABEL_MILLIS)
    override val totalMillis: Int = (glow.end + HOLD_MILLIS).toInt()

    private companion object {
        const val FALL_START = 300f
        const val SEED_STAGGER = 170f
        const val FALL_MILLIS = 700f
        const val RIPPLE_MILLIS = 500f
        const val SINK_DELAY = 60f
        const val SINK_MILLIS = 550f
        const val GLOW_OVERLAP = 150f
        const val GLOW_MILLIS = 1000f
    }
}

/**
 * La cruche s'incline et l'eau tombe en gouttes, la terre fonce, puis la plante repousse depuis la graine.
 * Sans changement de stade, la croissance est une courte évocation jusqu'au stade actuel ; avec un changement,
 * l'évocation va vite jusqu'à l'ancien stade et le passage au nouveau prend son temps, sous un halo doré.
 */
internal class WateringTimeline(val from: StageUi, val to: StageUi) : CeremonyTimeline {
    val hasGrown: Boolean = to > from
    val jugIn = Phase(200f, 650f)
    val tilt = Phase(450f, 900f)
    val pour = Phase(750f, 1600f)
    val tiltBack = Phase(1600f, 1950f)
    val jugOut = Phase(1750f, 2050f)
    val moisture = Phase(900f, 2100f)

    /** Croissance rapide depuis la graine jusqu'au stade de départ (ou jusqu'au stade actuel s'il ne change pas). */
    val evocation: Phase = run {
        val levels = if (hasGrown) from.level else to.level
        val perLevel = if (hasGrown) EVOCATION_PER_LEVEL_FAST else EVOCATION_PER_LEVEL
        Phase(GROWTH_START, GROWTH_START + levels * perLevel)
    }

    /** Passage au nouveau stade, mis en valeur ; vide si le stade ne change pas. */
    val emphasis: Phase = if (hasGrown) Phase(evocation.end, evocation.end + EMPHASIS_MILLIS) else Phase(evocation.end, evocation.end)

    val halo: Phase = if (hasGrown) {
        Phase(emphasis.end - HALO_LEAD, emphasis.end + HALO_TAIL)
    } else {
        Phase(evocation.end - HALO_LEAD / 2, evocation.end + HALO_TAIL + HALO_LEAD / 2)
    }
    override val label: Phase = Phase(emphasis.end, emphasis.end + LABEL_MILLIS)
    override val totalMillis: Int = (maxOf(halo.end, label.end) + HOLD_MILLIS).toInt()

    /** Taille de la plante, de 0 (graine) à 3 (fruit), au temps [t]. */
    fun growth(t: Float): Float {
        val evocated = (if (hasGrown) from.level else to.level) * FastOutSlowInEasing.transform(evocation.progress(t))
        if (!hasGrown) return evocated
        return evocated + (to.level - from.level) * FastOutSlowInEasing.transform(emphasis.progress(t))
    }

    private companion object {
        const val GROWTH_START = 1700f
        const val EVOCATION_PER_LEVEL = 450f
        const val EVOCATION_PER_LEVEL_FAST = 350f
        const val EMPHASIS_MILLIS = 1100f
        const val HALO_LEAD = 500f
        const val HALO_TAIL = 500f
    }
}

/** Échec, sans punition : la terre sèche, le feuillage s'incline, puis la plante revient au stade réel. */
internal class WiltingTimeline(val from: StageUi, val to: StageUi) : CeremonyTimeline {
    val dry = Phase(300f, 1300f)
    val droop = Phase(300f, 1200f)
    val crossfade = Phase(1100f, 1900f)
    override val label: Phase = Phase(1700f, 1700f + LABEL_MILLIS)
    override val totalMillis: Int = (label.end + HOLD_MILLIS).toInt()
}

/** Niveau de croissance d'un stade : 0 graine, 1 racine, 2 arbre, 3 fruit. */
internal val StageUi.level: Float get() = ordinal.toFloat()

/** Au-delà, les graines se chevaucheraient dans le cœur. */
internal const val MAX_SEEDS = 4
private const val FADE_IN_MILLIS = 300f
private const val LABEL_MILLIS = 400f
/** Le résultat reste un instant à l'écran avant le retour au jardin ou au bilan. */
private const val HOLD_MILLIS = 600f
