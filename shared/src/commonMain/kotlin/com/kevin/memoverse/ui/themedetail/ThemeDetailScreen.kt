package com.kevin.memoverse.ui.themedetail

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kevin.memoverse.presentation.themedetail.DifficultyUi
import com.kevin.memoverse.presentation.themedetail.StartErrorUi
import com.kevin.memoverse.presentation.themedetail.ThemeDetailEvent
import com.kevin.memoverse.presentation.themedetail.ThemeDetailUiState
import com.kevin.memoverse.presentation.themedetail.ThemeDetailViewModel
import com.kevin.memoverse.presentation.themedetail.VerseItemUi
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.back
import com.kevin.memoverse.resources.scene_parterres
import com.kevin.memoverse.resources.selection_count
import com.kevin.memoverse.resources.selection_limit_reached
import com.kevin.memoverse.resources.start_error_save_failed
import com.kevin.memoverse.resources.start_error_too_many_active
import com.kevin.memoverse.resources.start_learning
import com.kevin.memoverse.resources.theme_detail_instructions
import com.kevin.memoverse.resources.theme_garden_full
import com.kevin.memoverse.resources.theme_garden_full_short
import com.kevin.memoverse.resources.theme_go_water
import com.kevin.memoverse.resources.theme_verse_count
import com.kevin.memoverse.ui.components.ErrorState
import com.kevin.memoverse.ui.components.LoadingState
import com.kevin.memoverse.ui.components.PillButton
import com.kevin.memoverse.ui.components.VerseRow
import com.kevin.memoverse.ui.components.Wordmark
import com.kevin.memoverse.ui.icons.AppIcons
import com.kevin.memoverse.ui.theme.AppTheme
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.ScrimDark
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ThemeDetailScreen(
    themeId: String,
    onBack: () -> Unit,
    onLearningStarted: () -> Unit,
    onGardenClick: () -> Unit,
    viewModel: ThemeDetailViewModel = koinViewModel(key = themeId) { parametersOf(themeId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.isStarted) {
        if (state.isStarted) onLearningStarted()
    }
    ThemeDetailScreenContent(state = state, onEvent = viewModel::onEvent, onBack = onBack, onGardenClick = onGardenClick)
}

@Composable
fun ThemeDetailScreenContent(
    state: ThemeDetailUiState,
    onEvent: (ThemeDetailEvent) -> Unit,
    onBack: () -> Unit,
    onGardenClick: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    // Une fois l'image dépassée, la barre du haut devient opaque pour rester lisible au-dessus des versets.
    val isHeaderScrolledAway by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > HEADER_SCROLL_THRESHOLD }
    }
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        when {
            state.isLoading -> LoadingState()
            state.hasError -> ErrorState(onRetry = { onEvent(ThemeDetailEvent.RetryClicked) })
            else -> {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 140.dp)) {
                    item { ThemeHeader(state) }
                    item { VerseSheet(state = state, onEvent = onEvent) }
                }
                SelectionBar(
                    state = state,
                    onStartClick = { onEvent(ThemeDetailEvent.StartClicked) },
                    onGardenClick = onGardenClick,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
        TopBar(onBack = onBack, isOverImage = !state.isLoading && !state.hasError && !isHeaderScrolledAway)
    }
}

@Composable
private fun TopBar(onBack: () -> Unit, isOverImage: Boolean) {
    val tint = if (isOverImage) Color.White else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isOverImage) Color.Transparent else MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = if (isOverImage) 0.22f else 0.6f), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppIcons.Back, contentDescription = stringResource(Res.string.back), tint = tint, modifier = Modifier.size(20.dp))
        }
        Wordmark(color = tint, modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun ThemeHeader(state: ThemeDetailUiState) {
    Box(modifier = Modifier.fillMaxWidth().height(HEADER_HEIGHT)) {
        Image(
            painter = painterResource(Res.drawable.scene_parterres),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to ScrimDark.copy(alpha = 0.55f),
                        0.3f to Color.Transparent,
                        0.45f to Color.Transparent,
                        1f to ScrimDark.copy(alpha = 0.62f),
                    ),
                ),
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(start = Spacing.xl, end = Spacing.xl, bottom = Spacing.xxl + Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(state.themeName, style = MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.1f), color = Color.White)
            Text(
                text = pluralStringResource(Res.plurals.theme_verse_count, state.verses.size, state.verses.size),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
            Text(
                text = state.familyName,
                style = MaterialTheme.typography.titleLarge.copy(fontStyle = FontStyle.Italic),
                color = Color.White.copy(alpha = 0.9f),
            )
        }
    }
}

@Composable
private fun VerseSheet(state: ThemeDetailUiState, onEvent: (ThemeDetailEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = -Radius.sheet)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = Radius.sheet, topEnd = Radius.sheet))
            .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
    ) {
        if (state.isGardenFull) {
            Text(
                text = stringResource(Res.string.theme_garden_full, state.activeCount, state.maxActive),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f), RoundedCornerShape(Radius.row))
                    .padding(Spacing.md),
            )
        } else {
            Text(
                text = stringResource(Res.string.theme_detail_instructions, state.minSelection, state.maxSelection),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Spacing.xs),
            )
        }
        state.verses.forEachIndexed { index, verse ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            VerseRow(
                reference = verse.reference,
                text = verse.text,
                difficulty = verse.difficulty,
                isSelected = verse.isSelected,
                isLearning = verse.isLearning,
                enabled = !state.isGardenFull,
                onToggle = { onEvent(ThemeDetailEvent.VerseToggled(verse.id)) },
            )
        }
    }
}

@Composable
private fun SelectionBar(state: ThemeDetailUiState, onStartClick: () -> Unit, onGardenClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(percent = 50)
    val error = state.startError
    val message = when {
        error is StartErrorUi.TooManyActiveVerses -> stringResource(Res.string.start_error_too_many_active, error.freeSlots, error.maxActive)
        error == StartErrorUi.SaveFailed -> stringResource(Res.string.start_error_save_failed)
        state.isLimitReached -> stringResource(Res.string.selection_limit_reached, state.maxSelection)
        state.isGardenFull -> stringResource(Res.string.theme_garden_full_short)
        else -> pluralStringResource(Res.plurals.selection_count, state.selectedCount, state.selectedCount)
    }
    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md)
            .fillMaxWidth()
            .shadow(24.dp, shape, ambientColor = BarShadow, spotColor = BarShadow)
            .background(Color.White.copy(alpha = 0.94f), shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(start = Spacing.xl, end = Spacing.sm, top = Spacing.sm, bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = if (error != null || state.isLimitReached) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        // Jardin plein : planter est impossible, la seule suite utile est d'aller arroser.
        if (state.isGardenFull) {
            PillButton(text = stringResource(Res.string.theme_go_water), onClick = onGardenClick)
        } else {
            PillButton(text = stringResource(Res.string.start_learning), onClick = onStartClick, enabled = state.canStart, isLoading = state.isStarting)
        }
    }
}

private val HEADER_HEIGHT = 400.dp
private const val HEADER_SCROLL_THRESHOLD = 600
private val BarShadow = Color(0x44402E1A)

private val previewVerses = listOf(
    VerseItemUi("hebreux-11-1", "Hébreux 11:1", "Or la foi est une ferme assurance des choses qu’on espère…", "", DifficultyUi.EASY, isSelected = true),
    VerseItemUi("hebreux-11-6", "Hébreux 11:6", "Or sans la foi il est impossible de lui être agréable…", "", DifficultyUi.MEDIUM, isSelected = true),
    VerseItemUi("romains-10-17", "Romains 10:17", "Ainsi la foi vient de ce qu’on entend…", "", DifficultyUi.EASY, isSelected = false, isLearning = true),
    VerseItemUi("ephesiens-2-8-9", "Éphésiens 2:8-9", "Car c’est par la grâce que vous êtes sauvés…", "", DifficultyUi.HARD, isSelected = false),
)

@Preview
@Composable
private fun ThemeDetailScreenPreview() {
    AppTheme {
        ThemeDetailScreenContent(
            state = ThemeDetailUiState(
                isLoading = false,
                themeName = "Foi",
                familyName = "Relation avec Dieu",
                verses = previewVerses,
                selectedCount = 2,
                minSelection = 3,
                maxSelection = 4,
            ),
            onEvent = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun ThemeDetailScreenStartErrorPreview() {
    AppTheme {
        ThemeDetailScreenContent(
            state = ThemeDetailUiState(
                isLoading = false,
                themeName = "Paix",
                familyName = "Vie intérieure",
                verses = previewVerses,
                selectedCount = 3,
                minSelection = 3,
                maxSelection = 4,
                canStart = true,
                startError = StartErrorUi.TooManyActiveVerses(activeCount = 4, maxActive = 5),
            ),
            onEvent = {},
            onBack = {},
        )
    }
}
