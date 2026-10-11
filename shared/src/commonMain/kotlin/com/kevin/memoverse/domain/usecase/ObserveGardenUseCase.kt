package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.Garden
import com.kevin.memoverse.domain.model.GardenPlant
import com.kevin.memoverse.domain.repository.LearningRepository
import com.kevin.memoverse.domain.repository.TimeProvider
import com.kevin.memoverse.domain.repository.VerseBankRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/** Le jardin = les versets en apprentissage, avec leur référence (« Hébreux 11:1 ») et leur soif. */
class ObserveGardenUseCase(
    private val learningRepository: LearningRepository,
    private val verseBankRepository: VerseBankRepository,
    private val timeProvider: TimeProvider,
    /** Recalcule la soif au fil du temps : un verset peut arriver à échéance app ouverte. */
    private val ticks: Flow<Unit> = minuteTicks(),
) {
    operator fun invoke(): Flow<Garden> = combine(learningRepository.observeLearningVerses(), ticks) { learning, _ -> learning }.map { learning ->
        val references = when (val result = verseBankRepository.getVerses(learning.map { it.verseId })) {
            is DataResult.Success -> result.data.associate { it.id to it.reference }
            is DataResult.Error -> emptyMap()
        }
        val now = timeProvider.nowEpochMillis()
        Garden(
            plants = learning.map { GardenPlant(it.verseId, references[it.verseId] ?: it.verseId, it.stage, it.isThirsty(now)) },
            millisUntilNextWatering = learning
                .takeIf { verses -> verses.none { it.isThirsty(now) } }
                ?.minOfOrNull { it.dueAtEpochMillis - now },
        )
    }
}

private const val MINUTE_MILLIS = 60_000L

private fun minuteTicks(): Flow<Unit> = flow {
    while (true) {
        emit(Unit)
        delay(MINUTE_MILLIS)
    }
}
