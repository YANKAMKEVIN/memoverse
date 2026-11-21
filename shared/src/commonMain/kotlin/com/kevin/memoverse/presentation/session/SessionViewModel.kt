package com.kevin.memoverse.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.Exercise
import com.kevin.memoverse.domain.usecase.BuildWateringSessionUseCase
import com.kevin.memoverse.domain.usecase.CheckAnswerUseCase
import com.kevin.memoverse.domain.speech.SpeechError
import com.kevin.memoverse.domain.speech.SpeechEvent
import com.kevin.memoverse.domain.usecase.ListenToRecitationUseCase
import com.kevin.memoverse.domain.usecase.MeasureRecitationUseCase
import com.kevin.memoverse.domain.usecase.RecordExerciseResultUseCase
import com.kevin.memoverse.domain.usecase.RecordWateringSessionUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SessionViewModel(
    private val buildSession: BuildWateringSessionUseCase,
    private val checkAnswer: CheckAnswerUseCase,
    private val recordResult: RecordExerciseResultUseCase,
    private val listenToRecitation: ListenToRecitationUseCase,
    private val measureRecitation: MeasureRecitationUseCase,
    private val recordSession: RecordWateringSessionUseCase,
) : ViewModel() {

    private val model = MutableStateFlow(SessionModel())
    private var listeningJob: Job? = null

    val state: StateFlow<SessionUiState> = model
        .map { it.toUiState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SessionUiState())

    init {
        load()
    }

    fun onEvent(event: SessionEvent) {
        when (event) {
            is SessionEvent.OptionSelected -> editDraft { if (it is AnswerDraft.Choice) it.copy(selected = event.option) else it }
            is SessionEvent.ChunkPlaced -> editDraft { if (it is AnswerDraft.Order && event.chunkIndex !in it.placed) it.copy(placed = it.placed + event.chunkIndex) else it }
            is SessionEvent.ChunkRemoved -> editDraft { if (it is AnswerDraft.Order) it.copy(placed = it.placed - event.chunkIndex) else it }
            is SessionEvent.WordChoiceTapped -> editDraft { draft ->
                if (draft !is AnswerDraft.Blanks || event.choiceIndex in draft.filled) return@editDraft draft
                val slot = draft.filled.indexOfFirst { it == null }
                if (slot < 0) draft else draft.copy(filled = draft.filled.toMutableList().also { it[slot] = event.choiceIndex })
            }
            is SessionEvent.BlankTapped -> editDraft { draft ->
                if (draft is AnswerDraft.Blanks) draft.copy(filled = draft.filled.toMutableList().also { it[event.slot] = null }) else draft
            }
            is SessionEvent.TextChanged -> editDraft { if (it is AnswerDraft.Typed) it.copy(text = event.text, fromSpeech = false) else it }
            is SessionEvent.BlankTextChanged -> editDraft { draft ->
                if (draft is AnswerDraft.TypedBlanks) draft.copy(values = draft.values.toMutableList().also { it[event.slot] = event.text }) else draft
            }
            SessionEvent.MicClicked -> toggleListening()
            SessionEvent.MicPermissionDenied -> model.update { it.copy(speech = it.speech.copy(isListening = false, error = SpeechErrorUi.PERMISSION_DENIED)) }
            SessionEvent.TypeInsteadClicked -> {
                stopListening()
                model.update { m ->
                    val restored = m.speech.typedText.takeIf { it.isNotBlank() && (m.draft as? AnswerDraft.Typed)?.text.isNullOrBlank() }
                    m.copy(
                        draft = restored?.let { AnswerDraft.Typed(text = it) } ?: m.draft,
                        speech = m.speech.copy(isTyping = true, error = null, typedText = ""),
                    )
                }
            }
            // Ce qui avait été écrit ne compte pas comme récité : on repart d'une goutte vide, le texte est mis de côté.
            SessionEvent.SpeakInsteadClicked -> model.update { m ->
                if (m.lastResult != null) m else m.copy(draft = null, speech = SpeechModel(typedText = (m.draft as? AnswerDraft.Typed)?.takeUnless { it.fromSpeech }?.text.orEmpty()))
            }
            SessionEvent.CheckClicked -> check()
            SessionEvent.ContinueClicked -> next()
            SessionEvent.CeremonyFinished -> model.update { it.copy(isCeremonyDone = true) }
            SessionEvent.RetryClicked -> load()
            SessionEvent.CloseClicked -> model.update {
                if (it.hasRecordedAnswers && !it.isFinished) it.copy(isConfirmingQuit = true) else it.copy(shouldClose = true)
            }
            SessionEvent.QuitConfirmed -> {
                stopListening()
                model.update { it.copy(isConfirmingQuit = false, shouldClose = true) }
            }
            SessionEvent.QuitCancelled -> model.update { it.copy(isConfirmingQuit = false) }
        }
    }

    private fun editDraft(transform: (AnswerDraft) -> AnswerDraft) {
        model.update { m ->
            val exercise = m.current ?: return@update m
            if (m.lastResult != null) return@update m
            m.copy(draft = transform(m.draft ?: exercise.emptyDraft()))
        }
    }

    private fun toggleListening() {
        if (model.value.speech.isListening) {
            listenToRecitation.stop()
            return
        }
        listeningJob?.cancel()
        val previous = (model.value.draft as? AnswerDraft.Typed)?.takeIf { it.fromSpeech }?.text.orEmpty()
        model.update { it.copy(speech = SpeechModel(isListening = true, committedText = previous, typedText = it.speech.typedText)) }
        listeningJob = viewModelScope.launch {
            listenToRecitation().collect { event ->
                when (event) {
                    SpeechEvent.Listening -> Unit
                    is SpeechEvent.Partial -> {
                        val heard = recordSpeech(event.text)
                        // La goutte est pleine : inutile de continuer à écouter.
                        if (isRecitationComplete(heard)) listenToRecitation.stop()
                    }
                    is SpeechEvent.Final -> {
                        recordSpeech(event.text)
                        model.update { it.copy(speech = it.speech.copy(isListening = false)) }
                    }
                    is SpeechEvent.Failed -> model.update { it.copy(speech = it.speech.copy(isListening = false, error = event.error.toUi())) }
                }
            }
            model.update { it.copy(speech = it.speech.copy(isListening = false)) }
        }
    }

    /** Ajoute ce qui vient d'être entendu à ce qui avait déjà été dit ; renvoie le tout. */
    private fun recordSpeech(text: String): String {
        val heard = listOf(model.value.speech.committedText, text).filter { it.isNotBlank() }.joinToString(" ")
        editDraft { if (it is AnswerDraft.Typed) it.copy(text = heard, fromSpeech = true) else it }
        return heard
    }

    private fun isRecitationComplete(heard: String): Boolean {
        val exercise = model.value.current as? Exercise.ReciteAloud ?: return false
        return measureRecitation(exercise.verse.text, heard).isComplete
    }

    private fun stopListening() {
        listeningJob?.cancel()
        listeningJob = null
        // Le job annulé n'atteint jamais sa remise à zéro finale : sans ceci, « Je t'écoute… » resterait affiché.
        model.update { if (it.speech.isListening) it.copy(speech = it.speech.copy(isListening = false)) else it }
    }

    private fun SpeechError.toUi(): SpeechErrorUi = when (this) {
        SpeechError.PERMISSION_DENIED -> SpeechErrorUi.PERMISSION_DENIED
        SpeechError.UNAVAILABLE -> SpeechErrorUi.UNAVAILABLE
        SpeechError.LANGUAGE_PACK_MISSING -> SpeechErrorUi.LANGUAGE_PACK_MISSING
        SpeechError.NO_MATCH -> SpeechErrorUi.NO_MATCH
        SpeechError.OTHER -> SpeechErrorUi.OTHER
    }

    private fun check() {
        stopListening()
        val m = model.value
        val exercise = m.current ?: return
        if (m.lastResult != null) return
        val answer = (m.draft ?: exercise.emptyDraft()).toAnswer(exercise) ?: return
        val isCorrect = checkAnswer(exercise, answer)
        val wateredBefore = exercise.verse.id in m.wateredVerseIds
        model.update {
            it.copy(
                lastResult = ExerciseResult(isCorrect),
                correctCount = it.correctCount + if (isCorrect) 1 else 0,
                wateredVerseIds = if (isCorrect) it.wateredVerseIds + exercise.verse.id else it.wateredVerseIds,
            )
        }
        val verse = m.verses[exercise.verse.id] ?: return
        val step = m.index
        viewModelScope.launch {
            val outcome = when (val result = recordResult(verse, isCorrect)) {
                is DataResult.Success -> result.data
                is DataResult.Error -> {
                    // Rien n'a été enregistré : la réponse ne compte pas au bilan, et on le dit.
                    model.update { current ->
                        current.copy(
                            correctCount = current.correctCount - if (isCorrect) 1 else 0,
                            wateredVerseIds = if (isCorrect && !wateredBefore) current.wateredVerseIds - exercise.verse.id else current.wateredVerseIds,
                            lastResult = current.lastResult
                                ?.takeIf { current.index == step }
                                ?.copy(notSaved = true)
                                ?: current.lastResult,
                        )
                    }
                    return@launch
                }
            }
            model.update { current ->
                val result = current.lastResult.takeIf { current.index == step }
                current.copy(
                    verses = current.verses + (outcome.verse.verseId to outcome.verse),
                    lastResult = result?.copy(
                        grewTo = outcome.verse.stage.takeIf { outcome.hasGrown },
                        wiltedTo = outcome.verse.stage.takeIf { outcome.hasWilted },
                    ) ?: current.lastResult,
                )
            }
        }
    }

    private fun next() {
        val before = model.value
        model.update { m ->
            if (m.lastResult == null) return@update m
            val nextIndex = m.index + 1
            if (nextIndex >= m.exercises.size) m.copy(isFinished = true, lastResult = null, speech = SpeechModel())
            else m.copy(index = nextIndex, draft = null, lastResult = null, speech = SpeechModel())
        }
        if (!before.isFinished && model.value.isFinished) recordFinishedSession()
    }

    /** L'XP de la séance rejoint le cumul et la série de jours ; le bilan affiche alors le total. */
    private fun recordFinishedSession() {
        val xp = model.value.correctCount * XP_PER_CORRECT_ANSWER
        viewModelScope.launch {
            val result = recordSession(xp)
            if (result is DataResult.Success) model.update { it.copy(stats = result.data) }
        }
    }

    private fun load() {
        model.value = SessionModel(isLoading = true)
        viewModelScope.launch {
            model.value = when (val result = buildSession()) {
                is DataResult.Success -> SessionModel(
                    isLoading = false,
                    exercises = result.data.exercises,
                    verses = result.data.learningVerses.associateBy { it.verseId },
                    initialStages = result.data.learningVerses.associate { it.verseId to it.stage },
                )
                is DataResult.Error -> SessionModel(isLoading = false, hasError = true)
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
