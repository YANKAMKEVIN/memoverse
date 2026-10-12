package com.kevin.memoverse.domain.model

/**
 * Avancement d'une récitation orale : quels mots du verset (découpé aux espaces, ponctuation comprise)
 * ont été dits dans l'ordre, et la part du verset retrouvée.
 */
data class RecitationProgress(
    val recitedWords: List<Boolean>,
    val ratio: Double,
) {
    /** Assez de mots retrouvés pour réussir l'exercice. */
    val isComplete: Boolean get() = ratio >= SUCCESS_RATIO

    /** Remplissage de la goutte : pleine quand la récitation réussit. */
    val fill: Double get() = (ratio / SUCCESS_RATIO).coerceIn(0.0, 1.0)

    companion object {
        /** La transcription vocale déforme parfois un mot (« qu'on » → « con ») : 85 % suffisent. */
        const val SUCCESS_RATIO = 0.85
    }
}
