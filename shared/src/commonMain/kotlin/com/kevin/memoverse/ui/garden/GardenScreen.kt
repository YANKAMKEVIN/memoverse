package com.kevin.memoverse.ui.garden

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
import com.kevin.memoverse.presentation.garden.isNightHour
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
import com.kevin.memoverse.resources.scene_jardin_nuit
import com.kevin.memoverse.ui.components.PillButton
import com.kevin.memoverse.ui.components.PlantImage
import com.kevin.memoverse.ui.components.Wordmark
import com.kevin.memoverse.ui.components.stageName
import com.kevin.memoverse.ui.components.verseDueText
import com.kevin.memoverse.ui.theme.AppTextStyles
import com.kevin.memoverse.ui.theme.AppTheme
import com.kevin.memoverse.ui.theme.Cream
import com.kevin.memoverse.ui.theme.Gold
import com.kevin.memoverse.ui.theme.Ink
import com.kevin.memoverse.ui.theme.Olive
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.Sand
import com.kevin.memoverse.ui.theme.ScrimDark
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import com.kevin.memoverse.ui.components.textAction

@Composable
fun GardenScreen(
    contentPadding: PaddingValues,
    onPlantClick: () -> Unit,
    onWaterClick: () -> Unit,
    viewModel: GardenViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isNight by rememberIsNight()
    GardenScreenContent(
        state = state,
        contentPadding = contentPadding,
        onPlantClick = onPlantClick,
        onWaterClick = onWaterClick,
        onRetry = viewModel::onRetry,
        isNight = isNight,
    )
}

/** Vrai le soir et la nuit (heure locale), revérifié chaque minute tant que le jardin est affiché. */
@OptIn(ExperimentalTime::class)
@Composable
private fun rememberIsNight(): State<Boolean> = produceState(initialValue = currentHourIsNight()) {
    while (true) {
        value = currentHourIsNight()
        delay(NIGHT_CHECK_MILLIS)
    }
}

@OptIn(ExperimentalTime::class)
private fun currentHourIsNight(): Boolean =
    isNightHour(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour)

@Composable
fun GardenScreenContent(
    state: GardenUiState,
    contentPadding: PaddingValues,
    onPlantClick: () -> Unit,
    onWaterClick: () -> Unit,
    onRetry: () -> Unit = {},
    isNight: Boolean = false,
) {
    // Le soir, la scène de nuit ; en-tête et slogan passent en clair pour rester lisibles.
    val headerColor by animateColorAsState(if (isNight) Cream else Ink, tween(SCENE_FADE_MILLIS), label = "en-tête")
    // Verset touché dans la scène : sa fiche s'ouvre, avec les autres versets du jardin en dessous.
    var openedVerseId by rememberSaveable { mutableStateOf<String?>(null) }
    Box(modifier = Modifier.fillMaxSize().background(Sand)) {
        Crossfade(targetState = isNight, animationSpec = tween(SCENE_FADE_MILLIS), label = "scène") { night ->
            Image(
                painter = painterResource(if (night) Res.drawable.scene_jardin_nuit else Res.drawable.scene_jardin_jour),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        GardenPlants(plants = state.plants, isNight = isNight, onTagClick = { openedVerseId = it }, modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Spacing.xl, vertical = Spacing.md)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Wordmark(color = headerColor)
                // Poids et retour à la ligne : en grande police, le slogan ne chevauche plus le nom.
                Text(
                    text = stringResource(Res.string.app_tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = headerColor.copy(alpha = 0.8f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f).padding(start = Spacing.md),
                )
            }
            if (!state.isLoading) GardenHeader(state = state, color = headerColor, modifier = Modifier.padding(top = Spacing.xl))
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Sand.copy(alpha = 0f), (if (isNight) ScrimDark else Sand).copy(alpha = 0.85f))))
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
                        .textAction(onClick = onPlantClick)
                        .padding(horizontal = Spacing.lg),
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
        GardenVersesSheet(
            plants = state.plants,
            openedVerseId = verseId,
            thirstyCount = state.thirstyCount,
            onWaterClick = {
                openedVerseId = null
                onWaterClick()
            },
            onDismiss = { openedVerseId = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GardenVersesSheet(
    plants: List<GardenPlantUi>,
    openedVerseId: String,
    thirstyCount: Int,
    onWaterClick: () -> Unit,
    onDismiss: () -> Unit,
) {
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
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    // La plante du verset et son échéance : on voit d'un coup d'œil où il en est.
                    Box(modifier = Modifier.size(width = 44.dp, height = 56.dp), contentAlignment = Alignment.BottomCenter) {
                        PlantImage(stage = plant.stage, scale = SHEET_PLANT_SCALE, isThirsty = plant.isThirsty)
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                        Text(plant.reference, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "${stageName(plant.stage)} · ${verseDueText(plant)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        if (plant.text.isNotEmpty()) Text("«\u202F${plant.text}\u202F»", style = AppTextStyles.verseQuote())
                    }
                }
            }
            // On arrose là où l'on vient de voir qu'un verset a soif, sans refermer la fiche.
            if (thirstyCount > 0) {
                PillButton(
                    text = pluralStringResource(Res.plurals.garden_water_count, thirstyCount, thirstyCount),
                    onClick = onWaterClick,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
                )
            }
        }
    }
}

@Composable
private fun GardenHeader(state: GardenUiState, color: Color, modifier: Modifier = Modifier) {
    if (state.hasError) {
        Column(
            modifier = modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(stringResource(Res.string.garden_error_title), style = MaterialTheme.typography.headlineLarge, color = color)
            Text(stringResource(Res.string.garden_error_subtitle), style = MaterialTheme.typography.bodyMedium, color = color.copy(alpha = 0.75f))
        }
        return
    }
    if (!state.hasPlants) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(stringResource(Res.string.garden_empty_title), style = MaterialTheme.typography.headlineLarge, color = color)
            Text(stringResource(Res.string.garden_empty_subtitle), style = MaterialTheme.typography.bodyMedium, color = color.copy(alpha = 0.75f))
        }
        return
    }
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            Text(
                text = pluralStringResource(Res.plurals.garden_growing_count, state.plants.size, state.plants.size),
                style = MaterialTheme.typography.headlineLarge,
                color = color,
            )
            Text(stringResource(Res.string.garden_growing_label), style = MaterialTheme.typography.bodyMedium, color = color.copy(alpha = 0.75f))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            ActiveRing(active = state.activeCount, max = state.maxActive, color = color)
            Text(stringResource(Res.string.garden_active_label), style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.75f))
        }
    }
}

@Composable
private fun ActiveRing(active: Int, max: Int, color: Color) {
    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 3.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(color.copy(alpha = 0.15f), 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            val sweep = if (max == 0) 0f else 360f * active / max
            drawArc(if (color == Ink) Olive else color, -90f, sweep, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Text(stringResource(Res.string.garden_active_ratio, active, max), style = MaterialTheme.typography.headlineSmall.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize), color = color)
    }
}

/**
 * Les versets poussent sur les terrasses de la scène (positions relatives à l'illustration) :
 * chacun montre sa plante à son stade, avec son étiquette en dessous ; une plante qui a soif penche et pâlit.
 * Les versets qui ont soif passent en premier ; au-delà des emplacements, une pastille « +N ».
 */
@Composable
private fun GardenPlants(plants: List<GardenPlantUi>, isNight: Boolean, onTagClick: (verseId: String) -> Unit, modifier: Modifier = Modifier) {
    // La scène de nuit n'a pas ses terrasses aux mêmes endroits : chaque scène a ses emplacements.
    val slots = if (isNight) NIGHT_TAG_SLOTS else TAG_SLOTS
    val ordered = plants.sortedByDescending { it.isThirsty }
    val shown = ordered.take(if (ordered.size > slots.size) slots.size - 1 else slots.size)
    val hiddenCount = ordered.size - shown.size
    Box(modifier = modifier) {
        if (hiddenCount > 0) {
            val (fx, fy) = slots.last()
            PlantTag(
                text = stringResource(Res.string.garden_tag_more, hiddenCount),
                isThirsty = false,
                modifier = Modifier
                    .tagPosition(fx, fy)
                    .clickable(role = Role.Button, onClick = { onTagClick(ordered[shown.size].verseId) }),
            )
        }
        shown.forEachIndexed { index, plant ->
            val (fx, fy) = slots[index]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .tagPosition(fx, fy)
                    .clickable(role = Role.Button, onClick = { onTagClick(plant.verseId) }),
            ) {
                PlantImage(stage = plant.stage, isThirsty = plant.isThirsty, isNight = isNight)
                PlantTag(
                    text = if (plant.isThirsty) stringResource(Res.string.garden_tag_thirsty, plant.reference) else plant.reference,
                    isThirsty = plant.isThirsty,
                    modifier = Modifier.padding(top = Spacing.xxs),
                )
            }
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
            .background((if (isThirsty) Gold else Cream).copy(alpha = 0.94f), RoundedCornerShape(Radius.chip))
            .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(Radius.chip))
            .padding(horizontal = Spacing.sm + Spacing.xxs, vertical = Spacing.xs),
    )
}

/**
 * Place l'élément sur la scène, sans jamais le laisser déborder de l'écran : son bas (l'étiquette)
 * tombe juste sous la position relative, la plante pousse au-dessus.
 */
private fun Modifier.tagPosition(fx: Float, fy: Float, margin: Dp = TAG_EDGE_MARGIN): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    val edge = margin.roundToPx()
    val x = (constraints.maxWidth * fx).toInt().coerceAtMost(constraints.maxWidth - placeable.width - edge).coerceAtLeast(edge)
    val y = ((constraints.maxHeight * fy).toInt() + TAG_BASELINE.roundToPx() - placeable.height).coerceAtLeast(0)
    layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(x, y) }
}

private val TAG_EDGE_MARGIN = 12.dp
private const val SHEET_PLANT_SCALE = 0.17f

/** Hauteur d'une étiquette : le bas de chaque élément tombe là, quelle que soit la taille de la plante. */
private val TAG_BASELINE = 30.dp

/** Il faut de la place pour au moins 3 versets (le minimum d'une plantation). */
private const val MIN_FREE_SLOTS_TO_PLANT = 3

// Trois rangs espacés : la plus grande plante (Fruit) ne recouvre pas l'étiquette du rang du dessus.
private val TAG_SLOTS = listOf(
    0.56f to 0.44f,
    0.04f to 0.40f,
    0.30f to 0.56f,
    0.62f to 0.60f,
    0.06f to 0.68f,
    0.42f to 0.72f,
)

private val NIGHT_TAG_SLOTS = listOf(
    0.52f to 0.40f,
    0.05f to 0.40f,
    0.28f to 0.56f,
    0.64f to 0.56f,
    0.08f to 0.72f,
    0.48f to 0.72f,
)

private const val SCENE_FADE_MILLIS = 600
private const val NIGHT_CHECK_MILLIS = 60_000L

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
