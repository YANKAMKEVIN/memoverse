package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.exercise.ExerciseFactory
import com.kevin.memoverse.domain.exercise.ExercisePlanner
import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.WateringSession
import com.kevin.memoverse.domain.repository.LearningRepository
import com.kevin.memoverse.domain.repository.TimeProvider
import com.kevin.memoverse.domain.repository.VerseBankRepository
import kotlinx.coroutines.flow.first

/**
 * Prépare une séance : 2 exercices par verset qui a soif (5 versets au plus, les plus assoiffés
 * d'abord), entrelacés pour que le même verset ne revienne pas deux fois de suite.
 */
class BuildWateringSessionUseCase(
    private val learningRepository: LearningRepository,
    private val verseBankRepository: VerseBankRepository,
    private val exerciseFactory: ExerciseFactory,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(): DataResult<WateringSession> {
        val learning = learningRepository.observeLearningVerses().first()
        val now = timeProvider.nowEpochMillis()
        val growing = learning.filter { it.isThirsty(now) }.sortedBy { it.dueAtEpochMillis }.take(MAX_SESSION_VERSES)
        if (growing.isEmpty()) return DataResult.Success(WateringSession(emptyList(), emptyList()))
        val bank = when (val result = verseBankRepository.getAllVerses()) {
            is DataResult.Success -> result.data
            is DataResult.Error -> return result
        }
        val versesById = bank.associateBy { it.id }
        val planned = growing.mapNotNull { lv -> versesById[lv.verseId]?.let { verse -> Triple(verse, ExercisePlanner.levelsFor(lv), lv.successCount) } }
        val exercises = (0 until ExercisePlanner.EXERCISES_PER_VERSE).flatMap { round ->
            planned.map { (verse, levels, progress) -> exerciseFactory.create(levels[round], verse, bank, progress) }
        }
        return DataResult.Success(WateringSession(exercises, growing))
    }

    companion object {
        const val MAX_SESSION_VERSES = 5
    }
}
