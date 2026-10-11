package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.growth.GrowthRules
import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.GrowthOutcome
import com.kevin.memoverse.domain.model.LearningVerse
import com.kevin.memoverse.domain.repository.LearningRepository
import com.kevin.memoverse.domain.repository.TimeProvider
import com.kevin.memoverse.domain.review.ReviewScheduler

/** Applique la croissance et la répétition espacée au résultat d'un exercice, puis enregistre le verset. */
class RecordExerciseResultUseCase(
    private val repository: LearningRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(verse: LearningVerse, isCorrect: Boolean): DataResult<GrowthOutcome> {
        val now = timeProvider.nowEpochMillis()
        val grown = GrowthRules.apply(verse, isCorrect, now / MILLIS_PER_DAY)
        val updated = ReviewScheduler.apply(grown, isCorrect, now)
        return when (val result = repository.updateVerse(updated)) {
            is DataResult.Success -> DataResult.Success(GrowthOutcome(updated, verse.stage))
            is DataResult.Error -> result
        }
    }

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
    }
}
