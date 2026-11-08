package com.kevin.memoverse.ui.progression

import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kevin.memoverse.domain.model.LearningVerse
import com.kevin.memoverse.presentation.progression.ProgressionUiState
import com.kevin.memoverse.presentation.progression.ProgressionViewModel
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.progression_empty
import com.kevin.memoverse.resources.progression_fruit_caption
import com.kevin.memoverse.resources.progression_help_limit
import com.kevin.memoverse.resources.progression_help_stages
import com.kevin.memoverse.resources.progression_help_thirst
import com.kevin.memoverse.resources.progression_help_title
import com.kevin.memoverse.resources.progression_help_xp
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
import com.kevin.memoverse.ui.components.ErrorState
import com.kevin.memoverse.ui.components.LoadingState
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
    viewModel: ProgressionViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProgressionScreenContent(state = state, contentPadding = contentPadding, onRetry = viewModel::retry)
}

@Composable
fun ProgressionScreenContent(
    state: ProgressionUiState,
    contentPadding: PaddingValues,
    onRetry: () -> Unit = {},
) {
    if (state.isLoading) {
        LoadingState(Modifier.background(MaterialTheme.colorScheme.background))
        return
    }
    if (state.hasError) {
        ErrorState(onRetry = onRetry, modifier = Modifier.background(MaterialTheme.colorScheme.background))
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = contentPadding.calculateBottomPadding())
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Wordmark()
        Text(stringResource(Res.string.progression_title), style = MaterialTheme.typography.displayMedium, modifier = Modifier.padding(top = Spacing.sm))
        Text(stringResource(Res.string.progression_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.padding(top = Spacing.sm).height(IntrinsicSize.Min)) {
            StageCard(Res.string.stage_seed, state.seedCount, Res.string.progression_seed_caption, Res.drawable.stade_graine, Modifier.weight(1f).fillMaxHeight())
            StageCard(Res.string.stage_root, state.rootCount, Res.string.progression_root_caption, Res.drawable.stade_racine, Modifier.weight(1f).fillMaxHeight())
        }
        Row(modifier = Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            StageCard(Res.string.stage_tree, state.treeCount, Res.string.progression_tree_caption, Res.drawable.stade_arbre, Modifier.weight(1f).fillMaxHeight())
            StageCard(Res.string.stage_fruit, state.fruitCount, Res.string.progression_fruit_caption, Res.drawable.stade_fruit, Modifier.weight(1f).fillMaxHeight())
        }
        TotalCard(total = state.totalCount)
        if (state.totalCount == 0) {
            Text(
                text = stringResource(Res.string.progression_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        HelpCard()
    }
}

/** Les mots du jardin (stades, soif, limite, XP), expliqués une fois pour toutes. */
@Composable
private fun HelpCard() {
    val shape = RoundedCornerShape(Radius.card)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(stringResource(Res.string.progression_help_title), style = MaterialTheme.typography.titleMedium)
        listOf(
            stringResource(Res.string.progression_help_stages),
            stringResource(Res.string.progression_help_thirst),
            stringResource(Res.string.progression_help_limit, LearningVerse.MAX_ACTIVE_VERSES),
            stringResource(Res.string.progression_help_xp),
        ).forEach { line ->
            Text(line, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

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
            state = ProgressionUiState(isLoading = false, seedCount = 2, rootCount = 4, treeCount = 4, fruitCount = 2),
            contentPadding = PaddingValues(),
        )
    }
}
