package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.notification.ReviewNotificationPlanner
import com.kevin.memoverse.domain.repository.LearningRepository
import com.kevin.memoverse.domain.repository.NotificationScheduler
import com.kevin.memoverse.domain.repository.TimeProvider
import com.kevin.memoverse.domain.repository.VerseBankRepository
import kotlinx.datetime.TimeZone

/** Reprogramme les rappels à chaque changement du jardin (plantation, arrosage). */
class SyncReviewNotificationsUseCase(
    private val learningRepository: LearningRepository,
    private val verseBankRepository: VerseBankRepository,
    private val scheduler: NotificationScheduler,
    private val timeProvider: TimeProvider,
    private val timeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) {
    suspend operator fun invoke() {
        learningRepository.observeLearningVerses().collect { learning ->
            val references = when (val result = verseBankRepository.getVerses(learning.map { it.verseId })) {
                is DataResult.Success -> result.data.associate { it.id to it.reference }
                is DataResult.Error -> emptyMap()
            }
            val due = learning.map { ReviewNotificationPlanner.DueVerse(references[it.verseId] ?: it.verseId, it.dueAtEpochMillis) }
            scheduler.replaceAll(ReviewNotificationPlanner.plan(due, timeProvider.nowEpochMillis(), timeZone()))
        }
    }
}
