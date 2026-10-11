package com.kevin.memoverse.domain.model

/** Un exercice de séance, du plus facile (niveau 1) au plus dur (niveau 8). */
sealed interface Exercise {
    val verse: Verse

    /** Niveau 1 : retrouver la référence d'un verset parmi 4. */
    data class ChooseReference(
        override val verse: Verse,
        val options: List<String>,
    ) : Exercise

    /** Niveau 2 : remettre dans l'ordre des blocs de mots mélangés. */
    data class Reorder(
        override val verse: Verse,
        /** Blocs dans l'ordre du verset. */
        val chunks: List<String>,
        /** Indices de [chunks] dans l'ordre où ils sont proposés. */
        val shuffledOrder: List<Int>,
    ) : Exercise

    /** Niveau 3 : compléter les mots manquants à partir de propositions. */
    data class FillBlanks(
        override val verse: Verse,
        val words: List<String>,
        /** Positions des mots retirés dans [words], triées. */
        val blankIndexes: List<Int>,
        val choices: List<String>,
    ) : Exercise

    /** Niveau 4 : écrire soi-même les mots manquants (sans propositions). */
    data class TypeBlanks(
        override val verse: Verse,
        val words: List<String>,
        /** Positions des mots retirés dans [words], triées. */
        val blankIndexes: List<Int>,
    ) : Exercise

    /** Niveau 5 : retrouver la partie floutée parmi 3 propositions. */
    data class CompletePart(
        override val verse: Verse,
        val parts: VerseParts,
        val options: List<String>,
    ) : Exercise

    /** Niveau 6 : écrire la partie floutée. */
    data class TypePart(
        override val verse: Verse,
        val parts: VerseParts,
    ) : Exercise

    /** Niveau 7 : réciter (écrire) tout le verset de mémoire. */
    data class Recite(
        override val verse: Verse,
    ) : Exercise

    /** Niveau 8 : réciter le verset à voix haute (écrire reste possible). */
    data class ReciteAloud(
        override val verse: Verse,
    ) : Exercise
}

enum class HiddenPosition { START, MIDDLE, END }

/** Un verset coupé en trois, avec une partie cachée. */
data class VerseParts(
    val before: String,
    val hidden: String,
    val after: String,
    val position: HiddenPosition,
)

/** Réponse de l'utilisateur, à vérifier avec l'exercice correspondant. */
sealed interface ExerciseAnswer {
    data class Choice(val option: String) : ExerciseAnswer
    data class Order(val chunkIndexes: List<Int>) : ExerciseAnswer
    data class Blanks(val words: List<String>) : ExerciseAnswer
    data class Typed(val text: String) : ExerciseAnswer
    /** Transcription d'une récitation à voix haute (corrigée avec plus de tolérance). */
    data class Spoken(val text: String) : ExerciseAnswer
}
