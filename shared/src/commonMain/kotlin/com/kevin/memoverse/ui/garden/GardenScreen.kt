package com.kevin.memoverse.ui.garden

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kevin.memoverse.presentation.common.StageUi
import com.kevin.memoverse.presentation.garden.GardenPlantUi
import com.kevin.memoverse.presentation.garden.GardenUiState
import com.kevin.memoverse.presentation.garden.GardenViewModel
import com.kevin.memoverse.presentation.garden.NextWateringUi
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.app_tagline
import com.kevin.memoverse.resources.garden_active_label
import com.kevin.memoverse.resources.garden_active_ratio
import com.kevin.memoverse.resources.garden_empty_subtitle
import com.kevin.memoverse.resources.garden_error_subtitle
import com.kevin.memoverse.resources.garden_error_title
import com.kevin.memoverse.resources.garden_empty_title
import com.kevin.memoverse.resources.garden_growing_count
import com.kevin.memoverse.resources.garden_growing_label
import com.kevin.memoverse.resources.garden_next_watering_days
import com.kevin.memoverse.resources.garden_next_watering_hours
import com.kevin.memoverse.resources.garden_next_watering_minutes
import com.kevin.memoverse.resources.garden_plant_first
import com.kevin.memoverse.resources.garden_plant_more
import com.kevin.memoverse.resources.garden_sheet_title
import com.kevin.memoverse.resources.garden_tag_more
import com.kevin.memoverse.resources.garden_tag_thirsty
import com.kevin.memoverse.resources.garden_water_count
import com.kevin.memoverse.resources.garden_watered
import com.kevin.memoverse.resources.retry
import com.kevin.memoverse.resources.scene_jardin_jour
import com.kevin.memoverse.ui.components.PillButton
import com.kevin.memoverse.ui.components.Wordmark
import com.kevin.memoverse.ui.components.stageName
import com.kevin.memoverse.ui.theme.AppTextStyles
import com.kevin.memoverse.ui.theme.AppTheme
import com.kevin.memoverse.ui.theme.Gold
import com.kevin.memoverse.ui.theme.Ink
import com.kevin.memoverse.ui.theme.Olive
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.Sand
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GardenScreen(
    contentPadding: PaddingValues,
    onPlantClick: () -> Unit,
    onWaterClick: () -> Unit,
    viewModel: GardenViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    GardenScreenContent(
        state = state,
        contentPadding = contentPadding,
        onPlantClick = onPlantClick,
        onWaterClick = onWaterClick,
        onRetry = viewModel::onRetry,
    )
}

@Composable
fun GardenScreenContent(
    state: GardenUiState,
    contentPadding: PaddingValues,
    onPlantClick: () -> Unit,
    onWaterClick: () -> Unit,
    onRetry: () -> Unit = {},
) {
    // Verset touché dans la scène : sa fiche s'ouvre, avec les autres versets du jardin en dessous.
    var openedVerseId by rememberSaveable { mutableStateOf<String?>(null) }
    Box(modifier = Modifier.fillMaxSize().background(Sand)) {
        Image(
            painter = painterResource(Res.drawable.scene_jardin_jour),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        PlantTags(plants = state.plants, onTagClick = { openedVerseId = it }, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Spacing.xl, vertical = Spacing.md)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Wordmark()
                // Poids et retour à la ligne : en grande police, le slogan ne chevauche plus le nom.
                Text(
                    text = stringResource(Res.string.app_tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink.copy(alpha = 0.7f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f).padding(start = Spacing.md),
                )
            }
            if (!state.isLoading) GardenHeader(state = state, modifier = Modifier.padding(top = Spacing.xl))
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Sand.copy(alpha = 0f), Sand.copy(alpha = 0.85f))))
                .padding(contentPadding)
                .padding(horizontal = Spacing.xxl, vertical = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (state.hasError) {
                PillButton(text = stringResource(Res.string.retry), onClick = onRetry, modifier = Modifier.fillMaxWidth())
            } else if (state.hasPlants && state.maxActive - state.activeCount >= MIN_FREE_SLOTS_TO_PLANT) {
                Text(
                    text = stringResource(Res.string.garden_plant_more),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.75f), RoundedCornerShape(percent = 50))
                        .clickable(onClick = onPlantClick)
                        .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                )
            }
            if (state.thirstyCount > 0) {
                PillButton(
                    text = pluralStringResource(Res.plurals.garden_water_count, state.thirstyCount, state.thirstyCount),
                    onClick = onWaterClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else if (state.hasPlants) {
                val next = state.nextWatering
                Text(
                    text = when (next) {
                        is NextWateringUi.InMinutes -> pluralStringResource(Res.plurals.garden_next_watering_minutes, next.minutes, next.minutes)
                        is NextWateringUi.InHours -> pluralStringResource(Res.plurals.garden_next_watering_hours, next.hours, next.hours)
                        is NextWateringUi.InDays -> pluralStringResource(Res.plurals.garden_next_watering_days, next.days, next.days)
                        null -> stringResource(Res.string.garden_watered)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.75f), RoundedCornerShape(percent = 50))
                        .padding(vertical = Spacing.lg),
                )
            } else if (!state.isLoading && !state.hasError) {
                PillButton(text = stringResource(Res.string.garden_plant_first), onClick = onPlantClick, modifier = Modifier.fillMaxWidth())
            }
        }
    }
    openedVerseId?.let { verseId ->
        GardenVersesSheet(plants = state.plants, openedVerseId = verseId, onDismiss = { openedVerseId = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GardenVersesSheet(plants: List<GardenPlantUi>, openedVerseId: String, onDismiss: () -> Unit) {
    val ordered = plants.sortedByDescending { it.verseId == openedVerseId }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = Spacing.xl)
                .padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            Text(stringResource(Res.string.garden_sheet_title), style = MaterialTheme.typography.headlineMedium)
            ordered.forEach { plant ->
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                    Text(plant.reference, style = MaterialTheme.typography.titleLarge)
                    val stage = stageName(plant.stage)
                    Text(
                        text = if (plant.isThirsty) stringResource(Res.string.garden_tag_thirsty, stage) else stage,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (plant.text.isNotEmpty()) Text("« ${plant.text} »", style = AppTextStyles.verseQuote())
                }
            }
        }
    }
}

@Composable
private fun GardenHeader(state: GardenUiState, modifier: Modifier = Modifier) {
    if (state.hasError) {
        Column(
            modifier = modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(stringResource(Res.string.garden_error_title), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(Res.string.garden_error_subtitle), style = MaterialTheme.typography.bodyMedium, color = Ink.copy(alpha = 0.7f))
        }
        return
    }
    if (!state.hasPlants) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(stringResource(Res.string.garden_empty_title), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(Res.string.garden_empty_subtitle), style = MaterialTheme.typography.bodyMedium, color = Ink.copy(alpha = 0.7f))
        }
        return
    }
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            Text(
                text = pluralStringResource(Res.plurals.garden_growing_count, state.plants.size, state.plants.size),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(stringResource(Res.string.garden_growing_label), style = MaterialTheme.typography.bodyMedium, color = Ink.copy(alpha = 0.7f))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            ActiveRing(active = state.activeCount, max = state.maxActive)
            Text(stringResource(Res.string.garden_active_label), style = MaterialTheme.typography.labelSmall, color = Ink.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun ActiveRing(active: Int, max: Int) {
    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 3.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(Ink.copy(alpha = 0.12f), 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            val sweep = if (max == 0) 0f else 360f * active / max
            drawArc(Olive, -90f, sweep, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Text(stringResource(Res.string.garden_active_ratio, active, max), style = MaterialTheme.typography.headlineSmall.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize))
    }
}

/**
 * Étiquettes posées sur les terrasses de la scène (positions relatives à l'illustration).
 * Les versets qui ont soif passent en premier ; au-delà des emplacements, une pastille « +N ».
 */
@Composable
private fun PlantTags(plants: List<GardenPlantUi>, onTagClick: (verseId: String) -> Unit, modifier: Modifier = Modifier) {
    val ordered = plants.sortedByDescending { it.isThirsty }
    val shown = ordered.take(if (ordered.size > TAG_SLOTS.size) TAG_SLOTS.size - 1 else TAG_SLOTS.size)
    val hiddenCount = ordered.size - shown.size
    Box(modifier = modifier) {
        if (hiddenCount > 0) {
            val (fx, fy) = TAG_SLOTS.last()
            PlantTag(
                text = stringResource(Res.string.garden_tag_more, hiddenCount),
                isThirsty = false,
                onClick = { onTagClick(ordered[shown.size].verseId) },
                modifier = Modifier.tagPosition(fx, fy),
            )
        }
        shown.forEachIndexed { index, plant ->
            val (fx, fy) = TAG_SLOTS[index]
            PlantTag(
                text = if (plant.isThirsty) stringResource(Res.string.garden_tag_thirsty, plant.reference) else plant.reference,
                isThirsty = plant.isThirsty,
                onClick = { onTagClick(plant.verseId) },
                modifier = Modifier.tagPosition(fx, fy),
            )
        }
    }
}

@Composable
private fun PlantTag(text: String, isThirsty: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize),
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(Radius.chip))
            .background((if (isThirsty) Gold else Color(0xFFFBF7F0)).copy(alpha = 0.94f), RoundedCornerShape(Radius.chip))
            .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(Radius.chip))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Spacing.sm + Spacing.xxs, vertical = Spacing.xs),
    )
}

/** Place l'étiquette à sa position relative, sans jamais la laisser déborder de l'écran. */
private fun Modifier.tagPosition(fx: Float, fy: Float, margin: Dp = TAG_EDGE_MARGIN): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    val edge = margin.roundToPx()
    val x = (constraints.maxWidth * fx).toInt().coerceAtMost(constraints.maxWidth - placeable.width - edge).coerceAtLeast(edge)
    layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(x, (constraints.maxHeight * fy).toInt()) }
}

private val TAG_EDGE_MARGIN = 12.dp

/** Il faut de la place pour au moins 3 versets (le minimum d'une plantation). */
private const val MIN_FREE_SLOTS_TO_PLANT = 3

private val TAG_SLOTS = listOf(
    0.58f to 0.47f,
    0.17f to 0.51f,
    0.36f to 0.60f,
    0.04f to 0.42f,
    0.60f to 0.55f,
    0.10f to 0.64f,
)

@Preview
@Composable
private fun GardenScreenPreview() {
    AppTheme {
        GardenScreenContent(
            state = GardenUiState(
                isLoading = false,
                plants = listOf(
                    GardenPlantUi("hebreux-11-1", "Hébreux 11:1", StageUi.SEED, isThirsty = true),
                    GardenPlantUi("jean-3-16", "Jean 3:16", StageUi.ROOT),
                    GardenPlantUi("psaumes-23-1", "Psaumes 23:1", StageUi.SEED),
                ),
                activeCount = 3,
                maxActive = 5,
                thirstyCount = 1,
            ),
            contentPadding = PaddingValues(bottom = 80.dp),
            onPlantClick = {},
            onWaterClick = {},
        )
    }
}

@Preview
@Composable
private fun GardenScreenEmptyPreview() {
    AppTheme {
        GardenScreenContent(state = GardenUiState(isLoading = false, maxActive = 5), contentPadding = PaddingValues(), onPlantClick = {}, onWaterClick = {})
    }
}
