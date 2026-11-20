package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.WateringStats
import com.kevin.memoverse.domain.repository.PreferencesRepository
import com.kevin.memoverse.domain.repository.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone

/** L'XP cumulée et la série de jours telle qu'elle se lit aujourd'hui (0 si un jour a été manqué). */
class ObserveWateringStatsUseCase(
    private val preferences: PreferencesRepository,
    private val timeProvider: TimeProvider,
    private val timeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) {
    operator fun invoke(): Flow<WateringStats> = preferences.observeWateringStats().map { stats ->
        stats.copy(streakDays = stats.currentStreak(localEpochDay(timeProvider.nowEpochMillis(), timeZone())))
    }
}
