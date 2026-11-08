package com.kevin.memoverse.ui.ceremony

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.kevin.memoverse.presentation.ceremony.CeremonyUi
import com.kevin.memoverse.presentation.common.StageUi
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.ceremony_others_grown
import com.kevin.memoverse.resources.ceremony_planting_detail
import com.kevin.memoverse.resources.ceremony_planting_title
import com.kevin.memoverse.resources.ceremony_skip
import com.kevin.memoverse.resources.ceremony_skip_label
import com.kevin.memoverse.resources.ceremony_stage_change
import com.kevin.memoverse.resources.ceremony_watering_grew_title
import com.kevin.memoverse.resources.ceremony_watering_strengthen_title
import com.kevin.memoverse.resources.ceremony_wilting_title
import com.kevin.memoverse.ui.components.rememberReduceMotion
import com.kevin.memoverse.ui.components.stageName
import com.kevin.memoverse.ui.theme.AppTheme
import com.kevin.memoverse.ui.theme.Cream
import com.kevin.memoverse.ui.theme.Spacing
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Scène plein écran du jardin de ton cœur (plantation, arrosage ou verset qui fane), de 3 à 5 s.
 * Un tap la passe ; à la fin, elle appelle [onFinished] d'elle-même : elle ne bloque jamais le parcours.
 */
@Composable
fun GardenCeremony(
    ceremony: CeremonyUi,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeline = remember(ceremony) { CeremonyTimeline.of(ceremony) }
    val clock = remember(ceremony) { Animatable(0f) }
    val finish by rememberUpdatedState(onFinished)
    // Le retour passe la scène, comme un tap : la plantation ou l'arrosage est déjà enregistré.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        onBackCompleted = { finish() },
    )
    val reduceMotion = rememberReduceMotion()
    LaunchedEffect(ceremony) {
        if (reduceMotion) {
            // Réglage « réduire les animations » : la dernière image, le temps de la lire.
            clock.snapTo(timeline.totalMillis.toFloat())
            delay(REDUCED_MOTION_HOLD_MILLIS)
            finish()
            return@LaunchedEffect
        }
        // Une seule horloge, cadencée par les images : chaque étape lit le temps écoulé.
        clock.animateTo(timeline.totalMillis.toFloat(), tween(timeline.totalMillis, easing = LinearEasing))
        finish()
    }
    GardenCeremonyContent(ceremony = ceremony, timeline = timeline, time = { clock.value }, onSkip = onFinished, modifier = modifier)
}

@Composable
internal fun GardenCeremonyContent(
    ceremony: CeremonyUi,
    timeline: CeremonyTimeline,
    time: () -> Float,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val texts = ceremony.texts()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Cream)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = stringResource(Res.string.ceremony_skip_label),
                onClick = onSkip,
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.xl, vertical = Spacing.xl),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(bottom = Spacing.xl)
                .graphicsLayer { alpha = timeline.fadeIn.progress(time()) },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(
                text = texts.reference.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(texts.title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            // La scène se réduit plutôt que le texte, quand celui-ci est agrandi.
            CeremonyScene(
                timeline = timeline,
                time = time,
                modifier = Modifier.weight(1f, fill = false).widthIn(max = SCENE_MAX_WIDTH).fillMaxWidth().aspectRatio(1f / SCENE_RATIO),
            )
            Column(
                modifier = Modifier.graphicsLayer { alpha = timeline.label.progress(time()) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(texts.stage, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                texts.details.forEach { detail ->
                    Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
        }
        Text(
            text = stringResource(Res.string.ceremony_skip),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

private class CeremonyTexts(val reference: String, val title: String, val stage: String, val details: List<String>)

@Composable
private fun CeremonyUi.texts(): CeremonyTexts = when (this) {
    is CeremonyUi.Planting -> CeremonyTexts(
        reference = references.joinToString(" · "),
        title = pluralStringResource(Res.plurals.ceremony_planting_title, references.size, references.size),
        stage = stageName(StageUi.SEED),
        details = listOf(pluralStringResource(Res.plurals.ceremony_planting_detail, references.size)),
    )
    is CeremonyUi.Watering -> CeremonyTexts(
        reference = reference,
        title = stringResource(if (hasGrown) Res.string.ceremony_watering_grew_title else Res.string.ceremony_watering_strengthen_title),
        stage = stageName(to),
        details = listOfNotNull(
            if (hasGrown) stringResource(Res.string.ceremony_stage_change, stageName(from), stageName(to)) else null,
            if (otherGrownCount > 0) pluralStringResource(Res.plurals.ceremony_others_grown, otherGrownCount, otherGrownCount) else null,
        ),
    )
    is CeremonyUi.Wilting -> CeremonyTexts(
        reference = reference,
        title = stringResource(Res.string.ceremony_wilting_title),
        stage = stageName(to),
        details = listOf(stringResource(Res.string.ceremony_stage_change, stageName(from), stageName(to))),
    )
}

private val SCENE_MAX_WIDTH = 360.dp
private const val REDUCED_MOTION_HOLD_MILLIS = 2_000L

/** Aperçu figé à un instant donné de la scène (par défaut, sa dernière image). */
@Composable
private fun CeremonyPreview(ceremony: CeremonyUi, atMillis: Float? = null) {
    val timeline = remember(ceremony) { CeremonyTimeline.of(ceremony) }
    AppTheme {
        GardenCeremonyContent(ceremony, timeline, time = { atMillis ?: timeline.totalMillis.toFloat() }, onSkip = {})
    }
}

@Preview
@Composable
private fun PlantingCeremonyPreview() {
    CeremonyPreview(CeremonyUi.Planting(listOf("Hébreux 11:1", "Hébreux 11:6", "Romains 10:17")))
}

@Preview
@Composable
private fun PlantingCeremonyFallingPreview() {
    CeremonyPreview(CeremonyUi.Planting(listOf("Hébreux 11:1", "Hébreux 11:6", "Romains 10:17")), atMillis = 900f)
}

@Preview
@Composable
private fun WateringCeremonyPouringPreview() {
    CeremonyPreview(CeremonyUi.Watering("Jean 14:27", StageUi.SEED, StageUi.ROOT), atMillis = 1200f)
}

@Preview
@Composable
private fun WateringCeremonyRootPreview() {
    CeremonyPreview(CeremonyUi.Watering("Jean 14:27", StageUi.SEED, StageUi.ROOT, otherGrownCount = 2))
}

@Preview
@Composable
private fun WateringCeremonyFruitPreview() {
    CeremonyPreview(CeremonyUi.Watering("Psaume 23:1", StageUi.TREE, StageUi.FRUIT))
}

@Preview
@Composable
private fun WateringCeremonyTreePreview() {
    CeremonyPreview(CeremonyUi.Watering("Philippiens 4:13", StageUi.TREE, StageUi.TREE))
}

@Preview
@Composable
private fun WiltingCeremonyPreview() {
    CeremonyPreview(CeremonyUi.Wilting("Romains 8:28", StageUi.TREE, StageUi.ROOT))
}
