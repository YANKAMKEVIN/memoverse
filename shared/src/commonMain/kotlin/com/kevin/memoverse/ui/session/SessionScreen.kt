package com.kevin.memoverse.ui.session

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kevin.memoverse.presentation.common.StageUi
import com.kevin.memoverse.presentation.session.ExerciseUi
import com.kevin.memoverse.presentation.session.FeedbackUi
import com.kevin.memoverse.presentation.session.OptionUi
import com.kevin.memoverse.presentation.session.PartPositionUi
import com.kevin.memoverse.presentation.session.SessionEvent
import com.kevin.memoverse.presentation.session.SessionUiState
import com.kevin.memoverse.presentation.session.SessionViewModel
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.session_back_to_garden
import com.kevin.memoverse.resources.session_check
import com.kevin.memoverse.resources.session_close
import com.kevin.memoverse.resources.session_empty_subtitle
import com.kevin.memoverse.resources.session_empty_title
import com.kevin.memoverse.resources.session_quit_cancel
import com.kevin.memoverse.resources.session_quit_confirm
import com.kevin.memoverse.resources.session_quit_message
import com.kevin.memoverse.resources.session_quit_title
import com.kevin.memoverse.resources.session_title_fill_blanks
import com.kevin.memoverse.resources.session_title_part_end
import com.kevin.memoverse.resources.session_title_part_middle
import com.kevin.memoverse.resources.session_title_part_start
import com.kevin.memoverse.resources.session_title_recite
import com.kevin.memoverse.resources.session_title_recite_aloud
import com.kevin.memoverse.resources.session_title_type_blanks
import com.kevin.memoverse.resources.session_title_reference
import com.kevin.memoverse.resources.session_title_reorder
import com.kevin.memoverse.resources.session_title_type_end
import com.kevin.memoverse.resources.session_title_type_middle
import com.kevin.memoverse.resources.session_title_type_start
import com.kevin.memoverse.resources.vignette_exercice
import com.kevin.memoverse.ui.components.ErrorState
import com.kevin.memoverse.ui.components.LoadingState
import com.kevin.memoverse.ui.components.PillButton
import com.kevin.memoverse.ui.icons.AppIcons
import com.kevin.memoverse.ui.session.exercises.ChooseReferenceExercise
import com.kevin.memoverse.ui.session.exercises.CompletePartExercise
import com.kevin.memoverse.ui.session.exercises.FillBlanksExercise
import com.kevin.memoverse.ui.session.exercises.ReciteAloudExercise
import com.kevin.memoverse.ui.session.exercises.ReorderExercise
import com.kevin.memoverse.ui.session.exercises.TypeBlanksExercise
import com.kevin.memoverse.ui.session.exercises.TypeExercise
import com.kevin.memoverse.ui.theme.AppTheme
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.Sand
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SessionScreen(
    onClose: () -> Unit,
    viewModel: SessionViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.shouldClose) {
        if (state.shouldClose) onClose()
    }
    SessionScreenContent(state = state, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SessionScreenContent(
    state: SessionUiState,
    onEvent: (SessionEvent) -> Unit,
) {
    // Le retour Android passe par la même confirmation que la croix.
    BackHandler { onEvent(SessionEvent.CloseClicked) }
    val summary = state.summary
    when {
        state.isLoading -> LoadingState(Modifier.background(Sand))
        state.hasError -> ErrorState(onRetry = { onEvent(SessionEvent.RetryClicked) }, modifier = Modifier.background(Sand))
        summary != null -> SessionSummary(summary = summary, onContinue = { onEvent(SessionEvent.CloseClicked) })
        state.isEmpty -> EmptySession(onClose = { onEvent(SessionEvent.CloseClicked) })
        else -> ExerciseLayout(state = state, onEvent = onEvent)
    }
    if (state.isConfirmingQuit) {
        AlertDialog(
            onDismissRequest = { onEvent(SessionEvent.QuitCancelled) },
            title = { Text(stringResource(Res.string.session_quit_title)) },
            text = { Text(stringResource(Res.string.session_quit_message)) },
            confirmButton = {
                TextButton(onClick = { onEvent(SessionEvent.QuitConfirmed) }) { Text(stringResource(Res.string.session_quit_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(SessionEvent.QuitCancelled) }) { Text(stringResource(Res.string.session_quit_cancel)) }
            },
        )
    }
}

@Composable
private fun ExerciseLayout(state: SessionUiState, onEvent: (SessionEvent) -> Unit) {
    val exercise = state.exercise ?: return
    val feedback = state.feedback
    // imePadding : avec adjustResize, le contenu et la barre du bas remontent au-dessus du clavier.
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).imePadding()) {
        Box(modifier = Modifier.fillMaxWidth().height(HEADER_HEIGHT)) {
            Image(painterResource(Res.drawable.vignette_exercice), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Sand.copy(alpha = 0.9f), 0.55f to Sand.copy(alpha = 0f))))
        }
        Column(modifier = Modifier.fillMaxSize()) {
            SessionTopBar(step = state.step, total = state.totalSteps, onClose = { onEvent(SessionEvent.CloseClicked) })
            Spacer(Modifier.height(HEADER_HEIGHT - TOP_BAR_ALLOWANCE))
            val scrollState = rememberScrollState()
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = Radius.sheet, topEnd = Radius.sheet))
                    .verticalScroll(scrollState)
                    .padding(horizontal = Spacing.xl, vertical = Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                // La référence est la réponse de « Retrouve la référence » : on ne l'affiche pas avant validation.
                if (exercise !is ExerciseUi.ChooseReference || feedback != null) {
                    Text(exercise.reference.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(stringResource(exercise.titleRes()), style = MaterialTheme.typography.headlineMedium)
                val isLocked = feedback != null
                when (exercise) {
                    is ExerciseUi.ChooseReference -> ChooseReferenceExercise(exercise, isLocked, onEvent)
                    is ExerciseUi.CompletePart -> CompletePartExercise(exercise, isLocked, onEvent)
                    is ExerciseUi.Reorder -> ReorderExercise(exercise, isLocked, onEvent)
                    is ExerciseUi.FillBlanks -> FillBlanksExercise(exercise, isLocked, onEvent)
                    is ExerciseUi.Type -> TypeExercise(exercise, isLocked, onEvent)
                    is ExerciseUi.TypeBlanks -> TypeBlanksExercise(exercise, isLocked, onEvent)
                    is ExerciseUi.ReciteAloud -> ReciteAloudExercise(exercise, isLocked, onEvent)
                }
                // Assez de place pour lire la fin de l'exercice au-dessus du panneau de correction.
                if (feedback != null) Spacer(Modifier.height(FEEDBACK_ALLOWANCE))
            }
            // Fondu en bas tant qu'il reste du contenu : il invite à faire défiler.
            if (scrollState.canScrollForward) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(SCROLL_HINT_HEIGHT)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.surface))),
                )
            }
            }
            if (feedback == null) {
                // Barre fixe sous le contenu : le bouton ne recouvre jamais une proposition ni un champ.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .navigationBarsPadding()
                        .padding(horizontal = Spacing.xxl, vertical = Spacing.md),
                ) {
                    PillButton(
                        text = stringResource(Res.string.session_check),
                        onClick = { onEvent(SessionEvent.CheckClicked) },
                        enabled = state.canCheck,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        if (feedback != null) {
            FeedbackPanel(feedback = feedback, onContinue = { onEvent(SessionEvent.ContinueClicked) }, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun SessionTopBar(step: Int, total: Int, onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.7f), CircleShape).clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppIcons.Back, contentDescription = stringResource(Res.string.session_close), tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
        }
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            repeat(total) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = if (index < step) 1f else if (index == step) 0.55f else 0.18f), RoundedCornerShape(2.dp)),
                )
            }
        }
    }
}

@Composable
private fun EmptySession(onClose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Sand).padding(Spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(Res.string.session_empty_title), style = MaterialTheme.typography.headlineLarge)
        Text(stringResource(Res.string.session_empty_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        PillButton(text = stringResource(Res.string.session_back_to_garden), onClick = onClose)
    }
}

private fun ExerciseUi.titleRes(): StringResource = when (this) {
    is ExerciseUi.ChooseReference -> Res.string.session_title_reference
    is ExerciseUi.Reorder -> Res.string.session_title_reorder
    is ExerciseUi.FillBlanks -> Res.string.session_title_fill_blanks
    is ExerciseUi.TypeBlanks -> Res.string.session_title_type_blanks
    is ExerciseUi.ReciteAloud -> if (isTyping) Res.string.session_title_recite else Res.string.session_title_recite_aloud
    is ExerciseUi.CompletePart -> when (position) {
        PartPositionUi.START -> Res.string.session_title_part_start
        PartPositionUi.MIDDLE -> Res.string.session_title_part_middle
        PartPositionUi.END -> Res.string.session_title_part_end
    }
    is ExerciseUi.Type -> when (position) {
        PartPositionUi.START -> Res.string.session_title_type_start
        PartPositionUi.MIDDLE -> Res.string.session_title_type_middle
        PartPositionUi.END -> Res.string.session_title_type_end
        null -> Res.string.session_title_recite
    }
}

private val HEADER_HEIGHT = 260.dp
private val TOP_BAR_ALLOWANCE = 96.dp
private val FEEDBACK_ALLOWANCE = 280.dp
private val SCROLL_HINT_HEIGHT = 40.dp

private val previewPart = ExerciseUi.CompletePart(
    reference = "Hébreux 11:1",
    position = PartPositionUi.END,
    before = "Or la foi est une ferme assurance des choses qu’on espère,",
    hidden = "une démonstration de celles qu’on ne voit pas.",
    after = "",
    options = listOf(
        OptionUi("une lumière pour ceux qui marchent dans la nuit.", false),
        OptionUi("une démonstration de celles qu’on ne voit pas.", true),
        OptionUi("une espérance de celles qu’on attend encore.", false),
    ),
)

@Preview
@Composable
private fun SessionScreenPreview() {
    AppTheme {
        SessionScreenContent(SessionUiState(isLoading = false, step = 2, totalSteps = 6, exercise = previewPart, canCheck = true), onEvent = {})
    }
}

@Preview
@Composable
private fun SessionScreenFeedbackPreview() {
    AppTheme {
        SessionScreenContent(
            SessionUiState(
                isLoading = false,
                step = 2,
                totalSteps = 6,
                exercise = previewPart,
                feedback = FeedbackUi(isCorrect = true, reference = "Hébreux 11:1", verseText = "Or la foi est une ferme assurance des choses qu’on espère, une démonstration de celles qu’on ne voit pas.", grewTo = StageUi.ROOT),
            ),
            onEvent = {},
        )
    }
}
