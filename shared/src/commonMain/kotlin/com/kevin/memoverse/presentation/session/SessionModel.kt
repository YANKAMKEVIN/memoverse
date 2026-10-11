package com.kevin.memoverse.presentation.session

import com.kevin.memoverse.domain.model.Exercise
import com.kevin.memoverse.domain.model.ExerciseAnswer
import com.kevin.memoverse.domain.model.LearningVerse
import com.kevin.memoverse.domain.model.MasteryStage

/** État interne du ViewModel de séance ; traduit en [SessionUiState] par [toUiState]. */
internal data class SessionModel(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val exercises: List<Exercise> = emptyList(),
    val index: Int = 0,
    val draft: AnswerDraft? = null,
    val lastResult: ExerciseResult? = null,
    val verses: Map<String, LearningVerse> = emptyMap(),
    val initialStages: Map<String, MasteryStage> = emptyMap(),
    val correctCount: Int = 0,
    val isFinished: Boolean = false,
    val speech: SpeechModel = SpeechModel(),
    val isConfirmingQuit: Boolean = false,
    val shouldClose: Boolean = false,
) {
    /** Au moins un exercice a été validé : quitter demande une confirmation. */
    val hasRecordedAnswers: Boolean get() = index > 0 || lastResult != null
    val current: Exercise? get() = exercises.getOrNull(index)
}

/** État du micro pour l'exercice oral en cours. */
internal data class SpeechModel(
    val isListening: Boolean = false,
    val error: SpeechErrorUi? = null,
    val isTyping: Boolean = false,
)

internal data class ExerciseResult(
    val isCorrect: Boolean,
    val grewTo: MasteryStage? = null,
    val wiltedTo: MasteryStage? = null,
)

/** Réponse en cours de saisie, avant vérification. */
internal sealed interface AnswerDraft {
    data class Choice(val selected: String? = null) : AnswerDraft
    data class Order(val placed: List<Int> = emptyList()) : AnswerDraft
    data class Blanks(val filled: List<Int?>) : AnswerDraft
    data class Typed(val text: String = "", val fromSpeech: Boolean = false) : AnswerDraft
    data class TypedBlanks(val values: List<String>) : AnswerDraft
}

internal fun Exercise.emptyDraft(): AnswerDraft = when (this) {
    is Exercise.ChooseReference, is Exercise.CompletePart -> AnswerDraft.Choice()
    is Exercise.Reorder -> AnswerDraft.Order()
    is Exercise.FillBlanks -> AnswerDraft.Blanks(List(blankIndexes.size) { null })
    is Exercise.TypePart, is Exercise.Recite, is Exercise.ReciteAloud -> AnswerDraft.Typed()
    is Exercise.TypeBlanks -> AnswerDraft.TypedBlanks(List(blankIndexes.size) { "" })
}

/** Réponse complète prête à vérifier, ou null tant qu'il manque quelque chose. */
internal fun AnswerDraft.toAnswer(exercise: Exercise): ExerciseAnswer? = when (this) {
    is AnswerDraft.Choice -> selected?.let { ExerciseAnswer.Choice(it) }
    is AnswerDraft.Order -> (exercise as? Exercise.Reorder)
        ?.takeIf { placed.size == it.chunks.size }
        ?.let { ExerciseAnswer.Order(placed) }
    is AnswerDraft.Blanks -> (exercise as? Exercise.FillBlanks)
        ?.takeIf { filled.none { slot -> slot == null } }
        ?.let { fill -> ExerciseAnswer.Blanks(filled.map { fill.choices[it!!] }) }
    is AnswerDraft.Typed -> text.takeIf { it.isNotBlank() }?.let { if (fromSpeech) ExerciseAnswer.Spoken(it) else ExerciseAnswer.Typed(it) }
    is AnswerDraft.TypedBlanks -> values.takeIf { typed -> typed.all { it.isNotBlank() } }?.let { ExerciseAnswer.Blanks(it) }
}
