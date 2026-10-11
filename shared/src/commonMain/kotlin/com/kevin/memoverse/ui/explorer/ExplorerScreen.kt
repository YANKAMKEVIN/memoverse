package com.kevin.memoverse.ui.explorer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kevin.memoverse.presentation.themes.ThemeFamilyUi
import com.kevin.memoverse.presentation.themes.ThemeItemUi
import com.kevin.memoverse.presentation.themes.ThemesEvent
import com.kevin.memoverse.presentation.themes.ThemesUiState
import com.kevin.memoverse.presentation.themes.ThemesViewModel
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.explorer_subtitle
import com.kevin.memoverse.resources.explorer_title
import com.kevin.memoverse.resources.family_theme_count
import com.kevin.memoverse.ui.components.ErrorState
import com.kevin.memoverse.ui.components.LoadingState
import com.kevin.memoverse.ui.components.Wordmark
import com.kevin.memoverse.ui.components.familyIsland
import com.kevin.memoverse.ui.theme.AppTheme
import com.kevin.memoverse.ui.theme.Cream
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.Sand
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExplorerScreen(
    contentPadding: PaddingValues,
    onThemeClick: (themeId: String) -> Unit,
    viewModel: ThemesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ExplorerScreenContent(state = state, contentPadding = contentPadding, onEvent = viewModel::onEvent, onThemeClick = onThemeClick)
}

@Composable
fun ExplorerScreenContent(
    state: ThemesUiState,
    contentPadding: PaddingValues,
    onEvent: (ThemesEvent) -> Unit,
    onThemeClick: (themeId: String) -> Unit,
) {
    val background = Brush.verticalGradient(listOf(SkyWarm, Sand))
    Box(modifier = Modifier.fillMaxSize().background(background)) {
        when {
            state.isLoading -> LoadingState()
            state.hasError -> ErrorState(onRetry = { onEvent(ThemesEvent.RetryClicked) })
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + Spacing.xl),
            ) {
                item { ExplorerHeader(themeCount = state.families.sumOf { it.themes.size }) }
                itemsIndexed(state.families, key = { _, family -> family.name }) { index, family ->
                    FamilySection(family = family, isIslandOnLeft = index % 2 == 0, onThemeClick = onThemeClick)
                }
            }
        }
        // Fond derrière la barre d'état : le contenu qui défile ne passe pas sous l'heure.
        Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars).background(SkyWarm))
    }
}

@Composable
private fun ExplorerHeader(themeCount: Int) {
    Column(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = Spacing.md, bottom = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Wordmark()
        Text(stringResource(Res.string.explorer_title), style = MaterialTheme.typography.displayLarge, modifier = Modifier.padding(top = Spacing.md))
        Text(stringResource(Res.string.explorer_subtitle, themeCount), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FamilySection(
    family: ThemeFamilyUi,
    isIslandOnLeft: Boolean,
    onThemeClick: (themeId: String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.xl)) {
        Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            Image(
                painter = painterResource(familyIsland(family.name)),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(220.dp)
                    .align(if (isIslandOnLeft) Alignment.CenterStart else Alignment.CenterEnd)
                    .softEdges(),
            )
            FamilyLabel(
                family = family,
                modifier = Modifier.align(if (isIslandOnLeft) Alignment.BottomEnd else Alignment.BottomStart).padding(bottom = Spacing.xl),
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.padding(bottom = Spacing.lg),
        ) {
            family.themes.forEach { theme -> ThemeChip(theme = theme, onClick = { onThemeClick(theme.id) }) }
        }
    }
}

@Composable
private fun FamilyLabel(family: ThemeFamilyUi, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(Radius.row + Spacing.xxs)
    Column(
        modifier = modifier
            .shadow(16.dp, shape, ambientColor = ShadowWarm, spotColor = ShadowWarm)
            .background(Cream.copy(alpha = 0.95f), shape)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        Text(family.name, style = MaterialTheme.typography.titleLarge)
        Text(
            text = pluralStringResource(Res.plurals.family_theme_count, family.themes.size, family.themes.size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ThemeChip(theme: ThemeItemUi, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(percent = 50)
    Row(
        modifier = Modifier
            .background(colors.surface.copy(alpha = 0.9f), shape)
            .border(1.dp, colors.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(theme.name, style = MaterialTheme.typography.titleLarge.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize))
        if (theme.learningCount > 0) {
            Box(modifier = Modifier.size(20.dp).background(colors.primary, CircleShape), contentAlignment = Alignment.Center) {
                Text(theme.learningCount.toString(), style = MaterialTheme.typography.labelSmall, color = colors.onPrimary)
            }
        }
    }
}

/** Fond les bords de l'illustration dans le ciel (son fond crème ne se voit plus). */
private fun Modifier.softEdges(): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.radialGradient(0.62f to Color.Black, 1f to Color.Transparent),
            blendMode = BlendMode.DstIn,
        )
    }

private val SkyWarm = Color(0xFFF6E6D3)
private val ShadowWarm = Color(0x33402E1A)

@Preview
@Composable
private fun ExplorerScreenPreview() {
    AppTheme {
        ExplorerScreenContent(
            state = ThemesUiState(
                isLoading = false,
                families = listOf(
                    ThemeFamilyUi(
                        name = "Relation avec Dieu",
                        themes = listOf(ThemeItemUi("foi", "Foi", 12, learningCount = 3), ThemeItemUi("priere", "Prière", 10)),
                    ),
                    ThemeFamilyUi(name = "Promesses de Dieu", themes = listOf(ThemeItemUi("guerison", "Guérison", 12))),
                ),
            ),
            contentPadding = PaddingValues(),
            onEvent = {},
            onThemeClick = {},
        )
    }
}
