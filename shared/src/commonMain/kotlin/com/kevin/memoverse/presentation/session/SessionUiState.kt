package com.kevin.memoverse.presentation.session

import com.kevin.memoverse.presentation.common.StageUi

data class SessionUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val isEmpty: Boolean = false,
    val step: Int = 0,
    val totalSteps: Int = 0,
    val exercise: ExerciseUi? = null,
    val canCheck: Boolean = false,
    val feedback: FeedbackUi? = null,
    val summary: SessionSummaryUi? = null,
    val isConfirmingQuit: Boolean = false,
    /** La séance doit se fermer (quitter confirmé, ou rien n'avait encore été fait). */
    val shouldClose: Boolean = false,
)

enum class PartPositionUi { START, MIDDLE, END }

/** Après validation, chaque proposition révèle si c'était la bonne ([OptionVerdict]). */
data class OptionUi(val text: String, val isSelected: Boolean, val verdict: OptionVerdict? = null)

enum class OptionVerdict { CORRECT, WRONG_CHOICE }

data class ChunkUi(val index: Int, val text: String)

sealed interface SegmentUi {
    data class Word(val text: String) : SegmentUi
    data class Blank(val slot: Int, val filledWord: String?) : SegmentUi
}

sealed interface TypedSegmentUi {
    data class Word(val text: String) : TypedSegmentUi
    /** Trou à remplir au clavier ; [length] donne la largeur du champ. */
    data class Blank(val slot: Int, val value: String, val length: Int) : TypedSegmentUi
}

data class RecitedWordUi(val text: String, val state: RecitedWordState)

enum class RecitedWordState {
    /** Pas encore dit : affiché en pointillés. */
    HIDDEN,
    RECITED,
    /** Après validation : le mot qui n'a pas été dit, révélé pour qu'on le voie. */
    MISSED,
}

enum class SpeechErrorUi { PERMISSION_DENIED, UNAVAILABLE, LANGUAGE_PACK_MISSING, NO_MATCH, OTHER }

data class WordChoiceUi(val index: Int, val text: String, val isUsed: Boolean)

sealed interface ExerciseUi {
    val reference: String

    data class ChooseReference(override val reference: String, val verseText: String, val options: List<OptionUi>) : ExerciseUi
    data class Reorder(override val reference: String, val placed: List<ChunkUi>, val available: List<ChunkUi>) : ExerciseUi
    data class FillBlanks(override val reference: String, val segments: List<SegmentUi>, val choices: List<WordChoiceUi>) : ExerciseUi
    data class CompletePart(
        override val reference: String,
        val position: PartPositionUi,
        val before: String,
        val hidden: String,
        val after: String,
        val options: List<OptionUi>,
    ) : ExerciseUi

    data class TypeBlanks(override val reference: String, val segments: List<TypedSegmentUi>) : ExerciseUi

    data class ReciteAloud(
        override val reference: String,
        val transcript: String,
        /** Le verset mot à mot : chaque mot apparaît quand il a été dit. */
        val words: List<RecitedWordUi>,
        /** Remplissage de la goutte, de 0 à 1 (pleine = récitation réussie). */
        val fill: Float,
        val isListening: Boolean,
        val error: SpeechErrorUi?,
        val isTyping: Boolean,
    ) : ExerciseUi

    /** Écrire une partie floutée ([position] non nulle) ou tout le verset ([position] nulle). */
    data class Type(
        override val reference: String,
        val position: PartPositionUi?,
        val before: String,
        val hidden: String,
        val after: String,
        val text: String,
    ) : ExerciseUi
}

data class FeedbackUi(
    val isCorrect: Boolean,
    val reference: String,
    val verseText: String,
    /** Réponse attendue quand elle diffère du verset entier (référence, partie cachée). */
    val expectedAnswer: String? = null,
    val grewTo: StageUi? = null,
    val wiltedTo: StageUi? = null,
)

data class SessionSummaryUi(
    val xp: Int,
    val accuracyPercent: Int,
    val grownVerses: List<GrownVerseUi>,
    val wiltedVerses: List<GrownVerseUi> = emptyList(),
) {
    /** Moins d'une bonne réponse sur deux : le bilan encourage au lieu de féliciter. */
    val isEncouragement: Boolean get() = accuracyPercent < ENCOURAGEMENT_THRESHOLD_PERCENT

    private companion object {
        const val ENCOURAGEMENT_THRESHOLD_PERCENT = 50
    }
}

data class GrownVerseUi(val reference: String, val from: StageUi, val to: StageUi)
