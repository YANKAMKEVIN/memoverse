package com.kevin.memoverse.domain.model

data class LearningVerse(
    val verseId: String,
    val themeId: String,
    val stage: MasteryStage,
    val addedAtEpochMillis: Long,
    /** Réussites comptées dans le stade actuel. */
    val successCount: Int = 0,
    /** Jours distincts de réussite dans le stade actuel (règle du stade Racine). */
    val successDays: Int = 0,
    /** Jour (epoch) de la dernière réussite comptée dans le stade actuel. */
    val lastSuccessEpochDay: Long? = null,
    val consecutiveFailures: Int = 0,
    /** Cran sur l'échelle de révision (voir ReviewScheduler). */
    val reviewStep: Int = 0,
    /** Moment où le verset « a soif » et doit être arrosé ; 0 = tout de suite. */
    val dueAtEpochMillis: Long = 0,
) {
    fun isThirsty(nowEpochMillis: Long): Boolean = dueAtEpochMillis <= nowEpochMillis

    companion object {
        /** Cadrage : 5 versets actifs (Graine ou Racine) au maximum, pour ne pas se disperser. */
        const val MAX_ACTIVE_VERSES = 5
    }
}
