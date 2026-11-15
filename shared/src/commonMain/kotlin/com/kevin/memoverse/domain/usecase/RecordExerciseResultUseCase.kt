package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.growth.GrowthRules
import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.GrowthOutcome
import com.kevin.memoverse.domain.model.LearningVerse
import com.kevin.memoverse.domain.repository.LearningRepository
import com.kevin.memoverse.domain.repository.TimeProvider
import com.kevin.memoverse.domain.review.ReviewScheduler
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Applique la croissance et la répétition espacée au résultat d'un exercice, puis enregistre le verset. */
class RecordExerciseResultUseCase(
    private val repository: LearningRepository,
    private val timeProvider: TimeProvider,
    private val timeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) {
    suspend operator fun invoke(verse: LearningVerse, isCorrect: Boolean): DataResult<GrowthOutcome> {
        val now = timeProvider.nowEpochMillis()
        val grown = GrowthRules.apply(verse, isCorrect, localEpochDay(now))
        val updated = ReviewScheduler.apply(grown, isCorrect, now)
        return when (val result = repository.updateVerse(updated)) {
            is DataResult.Success -> DataResult.Success(GrowthOutcome(updated, verse.stage))
            is DataResult.Error -> result
        }
    }

    /** Jour calendaire de l'utilisateur (et non le jour UTC) : la règle des « jours distincts » suit son minuit. */
    @OptIn(ExperimentalTime::class)
    private fun localEpochDay(epochMillis: Long): Long =
        Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone()).date.toEpochDays()
}
