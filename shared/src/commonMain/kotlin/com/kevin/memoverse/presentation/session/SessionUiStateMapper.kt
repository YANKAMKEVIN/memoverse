package com.kevin.memoverse.presentation.session

import com.kevin.memoverse.domain.exercise.AnswerChecker
import com.kevin.memoverse.domain.model.Exercise
import com.kevin.memoverse.domain.model.HiddenPosition
import com.kevin.memoverse.domain.model.VerseParts
import com.kevin.memoverse.presentation.ceremony.CeremonyUi
import com.kevin.memoverse.presentation.common.toUi

/** Points d'expérience gagnés par bonne réponse. */
internal const val XP_PER_CORRECT_ANSWER = 10

internal fun SessionModel.toUiState(): SessionUiState {
    if (isLoading) return SessionUiState(isLoading = true)
    if (hasError) return SessionUiState(isLoading = false, hasError = true, shouldClose = shouldClose)
    if (exercises.isEmpty()) return SessionUiState(isLoading = false, isEmpty = true, shouldClose = shouldClose)
    if (isFinished) {
        val summary = summary()
        return SessionUiState(
            isLoading = false,
            step = exercises.size,
            totalSteps = exercises.size,
            summary = summary,
            ceremony = if (isCeremonyDone) null else ceremony(summary),
            shouldClose = shouldClose,
        )
    }
    val exercise = current ?: return SessionUiState(isLoading = false, isEmpty = true)
    val draft = draft ?: exercise.emptyDraft()
    return SessionUiState(
        isLoading = false,
        step = index,
        totalSteps = exercises.size,
        exercise = exercise.toUi(draft, speech, revealAnswer = lastResult != null),
        isConfirmingQuit = isConfirmingQuit,
        shouldClose = shouldClose,
        canCheck = lastResult == null && draft.toAnswer(exercise) != null,
        feedback = lastResult?.let { result ->
            FeedbackUi(
                isCorrect = result.isCorrect,
                reference = exercise.verse.reference,
                verseText = exercise.verse.text,
                expectedAnswer = exercise.expectedAnswer(),
                grewTo = result.grewTo?.toUi(),
                wiltedTo = result.wiltedTo?.toUi(),
                notSaved = result.notSaved,
            )
        },
    )
}

private fun Exercise.expectedAnswer(): String? = when (this) {
    is Exercise.ChooseReference -> verse.reference
    is Exercise.CompletePart -> parts.hidden
    is Exercise.TypePart -> parts.hidden
    is Exercise.TypeBlanks -> blankIndexes.joinToString(" · ") { words[it] }
    is Exercise.Reorder, is Exercise.FillBlanks, is Exercise.Recite, is Exercise.ReciteAloud -> null
}

private fun SessionModel.summary(): SessionSummaryUi = SessionSummaryUi(
    xp = correctCount * XP_PER_CORRECT_ANSWER,
    // Arrondi au plus proche : 4 sur 6 donnent 67 %, pas 66 %.
    accuracyPercent = if (exercises.isEmpty()) 0 else (correctCount * 100 + exercises.size / 2) / exercises.size,
    grownVerses = stageChanges { from, to -> to > from },
    wiltedVerses = stageChanges { from, to -> to < from },
)

/**
 * Une seule scène, pour ne pas rallonger la fin de séance : le verset qui a grandi le plus haut,
 * sinon un verset réussi qui garde son stade, sinon un verset qui a fané. Rien si la séance n'a rien changé.
 */
private fun SessionModel.ceremony(summary: SessionSummaryUi): CeremonyUi? {
    summary.grownVerses.maxByOrNull { it.to.ordinal }?.let { grown ->
        return CeremonyUi.Watering(grown.reference, grown.from, grown.to, otherGrownCount = summary.grownVerses.size - 1)
    }
    val watered = wateredVerseIds
        .mapNotNull { id -> verses[id]?.takeIf { verse -> initialStages[id]?.let { verse.stage >= it } ?: true } }
        .maxByOrNull { it.stage.ordinal }
    if (watered != null) {
        val stage = watered.stage.toUi()
        return CeremonyUi.Watering(referenceOf(watered.verseId), stage, stage)
    }
    return summary.wiltedVerses.firstOrNull()?.let { CeremonyUi.Wilting(it.reference, it.from, it.to) }
}

private fun SessionModel.referenceOf(verseId: String): String =
    exercises.firstOrNull { it.verse.id == verseId }?.verse?.reference ?: verseId

private fun SessionModel.stageChanges(keep: (from: Int, to: Int) -> Boolean): List<GrownVerseUi> =
    initialStages.mapNotNull { (verseId, from) ->
        val to = verses[verseId]?.stage ?: return@mapNotNull null
        if (!keep(from.ordinal, to.ordinal)) return@mapNotNull null
        GrownVerseUi(referenceOf(verseId), from.toUi(), to.toUi())
    }

internal fun Exercise.toUi(draft: AnswerDraft, speech: SpeechModel = SpeechModel(), revealAnswer: Boolean = false): ExerciseUi = when (this) {
    is Exercise.ChooseReference -> ExerciseUi.ChooseReference(verse.reference, verse.text, options.toOptions(draft, verse.reference.takeIf { revealAnswer }))
    is Exercise.CompletePart -> ExerciseUi.CompletePart(
        reference = verse.reference,
        position = parts.position.toUi(),
        before = parts.before,
        hidden = parts.hidden,
        after = parts.after,
        options = options.toOptions(draft, parts.hidden.takeIf { revealAnswer }),
    )
    is Exercise.Reorder -> {
        val placed = (draft as? AnswerDraft.Order)?.placed.orEmpty()
        ExerciseUi.Reorder(
            reference = verse.reference,
            placed = placed.map { ChunkUi(it, chunks[it]) },
            available = shuffledOrder.filter { it !in placed }.map { ChunkUi(it, chunks[it]) },
        )
    }
    is Exercise.FillBlanks -> {
        val filled = (draft as? AnswerDraft.Blanks)?.filled ?: List(blankIndexes.size) { null }
        ExerciseUi.FillBlanks(
            reference = verse.reference,
            segments = words.mapIndexed { i, word ->
                val slot = blankIndexes.indexOf(i)
                if (slot < 0) SegmentUi.Word(word) else SegmentUi.Blank(slot, filled[slot]?.let { choices[it] })
            },
            choices = choices.mapIndexed { i, word -> WordChoiceUi(i, word, isUsed = i in filled) },
        )
    }
    is Exercise.TypePart -> parts.toTypeUi(verse.reference, (draft as? AnswerDraft.Typed)?.text.orEmpty())
    is Exercise.TypeBlanks -> {
        val values = (draft as? AnswerDraft.TypedBlanks)?.values ?: List(blankIndexes.size) { "" }
        ExerciseUi.TypeBlanks(
            reference = verse.reference,
            segments = words.mapIndexed { i, word ->
                val slot = blankIndexes.indexOf(i)
                if (slot < 0) TypedSegmentUi.Word(word) else TypedSegmentUi.Blank(slot, values[slot], word.length)
            },
        )
    }
    is Exercise.ReciteAloud -> {
        val transcript = (draft as? AnswerDraft.Typed)?.text.orEmpty()
        val progress = AnswerChecker.recitationProgress(verse.text, transcript)
        ExerciseUi.ReciteAloud(
            reference = verse.reference,
            transcript = transcript,
            words = verse.text.split(WHITESPACE).filter { it.isNotEmpty() }.zip(progress.recitedWords) { word, isRecited ->
                RecitedWordUi(
                    text = word,
                    state = when {
                        isRecited -> RecitedWordState.RECITED
                        revealAnswer -> RecitedWordState.MISSED
                        else -> RecitedWordState.HIDDEN
                    },
                )
            },
            fill = progress.fill.toFloat(),
            isListening = speech.isListening,
            error = speech.error,
            isTyping = speech.isTyping,
        )
    }
    is Exercise.Recite -> ExerciseUi.Type(verse.reference, null, "", verse.text, "", (draft as? AnswerDraft.Typed)?.text.orEmpty())
}

private fun List<String>.toOptions(draft: AnswerDraft, correctAnswer: String?): List<OptionUi> = map { option ->
    val isSelected = (draft as? AnswerDraft.Choice)?.selected == option
    val verdict = when {
        correctAnswer == null -> null
        option == correctAnswer -> OptionVerdict.CORRECT
        isSelected -> OptionVerdict.WRONG_CHOICE
        else -> null
    }
    OptionUi(option, isSelected, verdict)
}

private fun VerseParts.toTypeUi(reference: String, text: String) =
    ExerciseUi.Type(reference, position.toUi(), before, hidden, after, text)

private fun HiddenPosition.toUi(): PartPositionUi = when (this) {
    HiddenPosition.START -> PartPositionUi.START
    HiddenPosition.MIDDLE -> PartPositionUi.MIDDLE
    HiddenPosition.END -> PartPositionUi.END
}

private val WHITESPACE = Regex("\\s+")
