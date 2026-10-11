package com.kevin.memoverse.domain.review

import com.kevin.memoverse.domain.model.LearningVerse

/**
 * Répétition espacée : chaque verset avance sur une échelle d'intervalles (cadrage : quelques
 * heures, puis 1, 3, 7, 21, 60 jours). Version simple et prévisible ; un vrai FSRS pourra la
 * remplacer sans toucher au reste.
 */
object ReviewScheduler {
    private const val HOUR_MILLIS = 3_600_000L
    private const val DAY_MILLIS = 24 * HOUR_MILLIS

    val STEPS_MILLIS = listOf(6 * HOUR_MILLIS, DAY_MILLIS, 3 * DAY_MILLIS, 7 * DAY_MILLIS, 21 * DAY_MILLIS, 60 * DAY_MILLIS, 120 * DAY_MILLIS)
    const val STEPS_BACK_ON_FAILURE = 2
    val RETRY_AFTER_FAILURE_MILLIS = 6 * HOUR_MILLIS

    /**
     * Une réussite ne fait avancer que si le verset avait soif : une 2e réussite dans la même
     * séance ne compte pas double. Un échec recule de 2 crans et fait revenir le verset bientôt.
     */
    fun apply(verse: LearningVerse, isCorrect: Boolean, nowEpochMillis: Long): LearningVerse = when {
        !isCorrect -> {
            val step = (verse.reviewStep - STEPS_BACK_ON_FAILURE).coerceAtLeast(0)
            verse.copy(reviewStep = step, dueAtEpochMillis = nowEpochMillis + RETRY_AFTER_FAILURE_MILLIS)
        }
        verse.isThirsty(nowEpochMillis) -> {
            val interval = STEPS_MILLIS[verse.reviewStep.coerceIn(0, STEPS_MILLIS.lastIndex)]
            verse.copy(
                reviewStep = (verse.reviewStep + 1).coerceAtMost(STEPS_MILLIS.lastIndex),
                dueAtEpochMillis = nowEpochMillis + interval,
            )
        }
        else -> verse
    }
}
