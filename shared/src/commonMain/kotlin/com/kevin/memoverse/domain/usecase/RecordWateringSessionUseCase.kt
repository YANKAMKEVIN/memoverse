package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.WateringStats
import com.kevin.memoverse.domain.repository.PreferencesRepository
import com.kevin.memoverse.domain.repository.TimeProvider
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone

/** Une séance d'arrosage est terminée : ajoute son XP au cumul et prolonge la série de jours. */
class RecordWateringSessionUseCase(
    private val preferences: PreferencesRepository,
    private val timeProvider: TimeProvider,
    private val timeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) {
    suspend operator fun invoke(xp: Int): DataResult<WateringStats> {
        val updated = preferences.observeWateringStats().first()
            .afterSession(xp, localEpochDay(timeProvider.nowEpochMillis(), timeZone()))
        return when (val result = preferences.saveWateringStats(updated)) {
            is DataResult.Success -> DataResult.Success(updated)
            is DataResult.Error -> result
        }
    }
}
