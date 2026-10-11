package com.kevin.memoverse.ui.garden

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import com.kevin.memoverse.resources.garden_empty_title
import com.kevin.memoverse.resources.garden_growing_count
import com.kevin.memoverse.resources.garden_growing_label
import com.kevin.memoverse.resources.garden_plant_first
import com.kevin.memoverse.resources.garden_plant_more
import com.kevin.memoverse.resources.garden_tag_thirsty
import com.kevin.memoverse.resources.garden_next_watering_days
import com.kevin.memoverse.resources.garden_next_watering_hours
import com.kevin.memoverse.resources.garden_next_watering_minutes
import com.kevin.memoverse.resources.garden_tag_more
import com.kevin.memoverse.resources.garden_water_count
import com.kevin.memoverse.resources.garden_watered
import com.kevin.memoverse.resources.scene_jardin_jour
import com.kevin.memoverse.ui.components.PillButton
import com.kevin.memoverse.ui.components.Wordmark
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
    GardenScreenContent(state = state, contentPadding = contentPadding, onPlantClick = onPlantClick, onWaterClick = onWaterClick)
}

@Composable
fun GardenScreenContent(
    state: GardenUiState,
    contentPadding: PaddingValues,
    onPlantClick: () -> Unit,
    onWaterClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().background(Sand)) {
        Image(
            painter = painterResource(Res.drawable.scene_jardin_jour),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        PlantTags(plants = state.plants, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Spacing.xl, vertical = Spacing.md)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Wordmark()
                Text(stringResource(Res.string.app_tagline), style = MaterialTheme.typography.bodySmall, color = Ink.copy(alpha = 0.7f))
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
            if (state.hasPlants && state.maxActive - state.activeCount >= MIN_FREE_SLOTS_TO_PLANT) {
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
            } else if (!state.isLoading) {
                PillButton(text = stringResource(Res.string.garden_plant_first), onClick = onPlantClick, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun GardenHeader(state: GardenUiState, modifier: Modifier = Modifier) {
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
private fun PlantTags(plants: List<GardenPlantUi>, modifier: Modifier = Modifier) {
    val ordered = plants.sortedByDescending { it.isThirsty }
    val shown = ordered.take(if (ordered.size > TAG_SLOTS.size) TAG_SLOTS.size - 1 else TAG_SLOTS.size)
    val hiddenCount = ordered.size - shown.size
    BoxWithConstraints(modifier = modifier) {
        if (hiddenCount > 0) {
            val (fx, fy) = TAG_SLOTS.last()
            PlantTag(
                text = stringResource(Res.string.garden_tag_more, hiddenCount),
                isThirsty = false,
                modifier = Modifier.offset(x = maxWidth * fx, y = maxHeight * fy),
            )
        }
        shown.forEachIndexed { index, plant ->
            val (fx, fy) = TAG_SLOTS[index]
            PlantTag(
                text = if (plant.isThirsty) stringResource(Res.string.garden_tag_thirsty, plant.reference) else plant.reference,
                isThirsty = plant.isThirsty,
                modifier = Modifier.offset(x = maxWidth * fx, y = maxHeight * fy),
            )
        }
    }
}

@Composable
private fun PlantTag(text: String, isThirsty: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize),
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(Radius.chip))
            .background((if (isThirsty) Gold else Color(0xFFFBF7F0)).copy(alpha = 0.94f), RoundedCornerShape(Radius.chip))
            .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(Radius.chip))
            .padding(horizontal = Spacing.sm + Spacing.xxs, vertical = Spacing.xs),
    )
}

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
