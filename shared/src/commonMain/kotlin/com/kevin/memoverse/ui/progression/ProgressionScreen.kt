package com.kevin.memoverse.ui.progression

import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.kevin.memoverse.presentation.common.StageUi
import com.kevin.memoverse.presentation.garden.GardenPlantUi
import com.kevin.memoverse.presentation.garden.NextWateringUi
import com.kevin.memoverse.presentation.progression.StageGroupUi
import com.kevin.memoverse.resources.garden_plant_first
import com.kevin.memoverse.resources.progression_empty_subtitle
import com.kevin.memoverse.resources.progression_empty_title
import com.kevin.memoverse.resources.progression_hide_text
import com.kevin.memoverse.resources.progression_list_title
import com.kevin.memoverse.resources.progression_streak_label
import com.kevin.memoverse.resources.progression_streak_value
import com.kevin.memoverse.resources.progression_xp_label
import com.kevin.memoverse.resources.progression_xp_value
import com.kevin.memoverse.resources.progression_show_text
import com.kevin.memoverse.ui.components.PillButton
import com.kevin.memoverse.ui.components.PlantImage
import com.kevin.memoverse.ui.components.stageName
import com.kevin.memoverse.ui.components.verseDueText
import com.kevin.memoverse.ui.theme.AppTextStyles
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kevin.memoverse.presentation.progression.ProgressionUiState
import com.kevin.memoverse.presentation.progression.ProgressionViewModel
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.progression_fruit_caption
import com.kevin.memoverse.resources.progression_root_caption
import com.kevin.memoverse.resources.progression_seed_caption
import com.kevin.memoverse.resources.progression_subtitle
import com.kevin.memoverse.resources.progression_title
import com.kevin.memoverse.resources.progression_total_label
import com.kevin.memoverse.resources.progression_total_value
import com.kevin.memoverse.resources.progression_tree_caption
import com.kevin.memoverse.resources.stade_arbre
import com.kevin.memoverse.resources.stade_fruit
import com.kevin.memoverse.resources.stade_graine
import com.kevin.memoverse.resources.stade_racine
import com.kevin.memoverse.resources.stage_fruit
import com.kevin.memoverse.resources.stage_root
import com.kevin.memoverse.resources.stage_seed
import com.kevin.memoverse.resources.stage_tree
import com.kevin.memoverse.ui.components.Wordmark
import com.kevin.memoverse.ui.theme.AppTheme
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProgressionScreen(
    contentPadding: PaddingValues,
    onPlantClick: () -> Unit,
    viewModel: ProgressionViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProgressionScreenContent(state = state, contentPadding = contentPadding, onPlantClick = onPlantClick)
}

@Composable
fun ProgressionScreenContent(
    state: ProgressionUiState,
    contentPadding: PaddingValues,
    onPlantClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(bottom = contentPadding.calculateBottomPadding())
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Wordmark()
        Text(stringResource(Res.string.progression_title), style = MaterialTheme.typography.displayMedium, modifier = Modifier.padding(top = Spacing.sm))
        Text(stringResource(Res.string.progression_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        // Jardin vide : une invitation à planter plutôt que quatre « 0 ».
        if (!state.isLoading && state.totalCount == 0) {
            EmptyProgression(onPlantClick = onPlantClick)
        } else {
            // En tête : ce que les arrosages ont rapporté, et la série qui donne envie de revenir demain.
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.padding(top = Spacing.sm).height(IntrinsicSize.Min)) {
                RewardCard(stringResource(Res.string.progression_xp_value, state.totalXp), stringResource(Res.string.progression_xp_label), Modifier.weight(1f).fillMaxHeight())
                RewardCard(
                    value = pluralStringResource(Res.plurals.progression_streak_value, state.streakDays, state.streakDays),
                    label = stringResource(Res.string.progression_streak_label),
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.height(IntrinsicSize.Min)) {
                StageCard(Res.string.stage_seed, state.seedCount, Res.string.progression_seed_caption, Res.drawable.stade_graine, Modifier.weight(1f).fillMaxHeight())
                StageCard(Res.string.stage_root, state.rootCount, Res.string.progression_root_caption, Res.drawable.stade_racine, Modifier.weight(1f).fillMaxHeight())
            }
            Row(modifier = Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                StageCard(Res.string.stage_tree, state.treeCount, Res.string.progression_tree_caption, Res.drawable.stade_arbre, Modifier.weight(1f).fillMaxHeight())
                StageCard(Res.string.stage_fruit, state.fruitCount, Res.string.progression_fruit_caption, Res.drawable.stade_fruit, Modifier.weight(1f).fillMaxHeight())
            }
            TotalCard(total = state.totalCount)
            if (state.groups.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.progression_list_title),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
                )
            }
            state.groups.forEach { group -> StageGroup(group) }
        }
    }
}

@Composable
private fun EmptyProgression(onPlantClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.xxl, start = Spacing.md, end = Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        PlantImage(stage = StageUi.SEED, scale = EMPTY_PLANT_SCALE)
        Text(stringResource(Res.string.progression_empty_title), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text(
            text = stringResource(Res.string.progression_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        PillButton(text = stringResource(Res.string.garden_plant_first), onClick = onPlantClick, modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm))
    }
}

/** Un stade et ses versets ; chaque ligne s'ouvre sur le texte complet. */
@Composable
private fun StageGroup(group: StageGroupUi) {
    val shape = RoundedCornerShape(Radius.card)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
    ) {
        Text(
            text = stageName(group.stage),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = Spacing.xs),
        )
        group.plants.forEachIndexed { index, plant ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            VerseProgressRow(plant)
        }
    }
}

@Composable
private fun VerseProgressRow(plant: GardenPlantUi) {
    var isExpanded by rememberSaveable(plant.verseId) { mutableStateOf(false) }
    val toggleLabel = stringResource(if (isExpanded) Res.string.progression_hide_text else Res.string.progression_show_text)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = toggleLabel, role = Role.Button) { isExpanded = !isExpanded }
            .padding(vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(width = 40.dp, height = 48.dp), contentAlignment = Alignment.BottomCenter) {
                PlantImage(stage = plant.stage, scale = ROW_PLANT_SCALE, isThirsty = plant.isThirsty)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                Text(plant.reference, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = verseDueText(plant).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (plant.isThirsty) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        AnimatedVisibility(visible = isExpanded && plant.text.isNotEmpty()) {
            Text("« ${plant.text} »", style = AppTextStyles.verseQuote(), modifier = Modifier.padding(top = Spacing.xs))
        }
    }
}

private const val EMPTY_PLANT_SCALE = 0.7f
private const val ROW_PLANT_SCALE = 0.15f

@Composable
private fun StageCard(
    name: StringResource,
    count: Int,
    caption: StringResource,
    illustration: DrawableResource,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Radius.card)
    // Hauteur minimale et non fixe : en grande police, la carte s'allonge au lieu d'empiler texte et image.
    Column(
        modifier = modifier
            .heightIn(min = 170.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                Text(stringResource(name), style = MaterialTheme.typography.titleMedium)
                Text(count.toString(), style = MaterialTheme.typography.headlineLarge)
            }
            Image(
                painter = painterResource(illustration),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(width = 56.dp, height = 90.dp),
            )
        }
        Text(
            text = stringResource(caption),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RewardCard(value: String, label: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(Radius.card)
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun TotalCard(total: Int) {
    val shape = RoundedCornerShape(Radius.card)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        Text(stringResource(Res.string.progression_total_label), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(pluralStringResource(Res.plurals.progression_total_value, total, total), style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview
@Composable
private fun ProgressionScreenPreview() {
    AppTheme {
        ProgressionScreenContent(
            state = ProgressionUiState(
                isLoading = false,
                groups = listOf(
                    StageGroupUi(StageUi.SEED, listOf(GardenPlantUi("hebreux-11-1", "Hébreux 11:1", StageUi.SEED, isThirsty = true, text = "Or la foi est une ferme assurance des choses qu’on espère."))),
                    StageGroupUi(StageUi.ROOT, listOf(GardenPlantUi("jean-3-16", "Jean 3:16", StageUi.ROOT, nextWatering = NextWateringUi.InDays(2)))),
                ),
            ),
            contentPadding = PaddingValues(),
        )
    }
}
