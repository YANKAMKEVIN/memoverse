package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.LearningVerse
import com.kevin.memoverse.domain.model.LearningVerse.Companion.MAX_ACTIVE_VERSES
import com.kevin.memoverse.domain.model.MasteryStage
import com.kevin.memoverse.domain.model.StartLearningResult
import com.kevin.memoverse.domain.model.VerseSelection
import com.kevin.memoverse.domain.repository.LearningRepository
import com.kevin.memoverse.domain.repository.TimeProvider
import kotlinx.coroutines.flow.first

/** Enregistre les versets choisis comme « Graine », en respectant le plafond de versets actifs. */
class StartLearningUseCase(
    private val repository: LearningRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(themeId: String, verseIds: Set<String>): StartLearningResult {
        if (verseIds.size !in VerseSelection.MIN_VERSES..VerseSelection.MAX_VERSES) {
            return StartLearningResult.InvalidSelection
        }
        val learning = repository.observeLearningVerses().first()
        val newIds = verseIds - learning.map { it.verseId }.toSet()
        val activeCount = learning.count { it.stage.isActive }
        if (activeCount + newIds.size > MAX_ACTIVE_VERSES) {
            return StartLearningResult.TooManyActiveVerses(activeCount, MAX_ACTIVE_VERSES)
        }
        val now = timeProvider.nowEpochMillis()
        val verses = newIds.map { LearningVerse(it, themeId, MasteryStage.SEED, now, dueAtEpochMillis = now) }
        return when (val result = repository.addVerses(verses)) {
            is DataResult.Success -> StartLearningResult.Started(newIds.size)
            is DataResult.Error -> StartLearningResult.Failed(result.cause)
        }
    }
}
