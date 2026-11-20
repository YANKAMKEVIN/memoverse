package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.WateringStats
import com.kevin.memoverse.fake.FakePreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveWateringStatsUseCaseTest {

    private val day = 86_400_000L
    private val preferences = FakePreferencesRepository().apply {
        wateringStats.value = WateringStats(totalXp = 420, streakDays = 3, lastSessionEpochDay = 20_000)
    }

    @Test
    fun `la série reste affichée le lendemain de la dernière séance`() = runTest {
        val stats = ObserveWateringStatsUseCase(preferences, { 20_001 * day + 1_000 }, { TimeZone.UTC })().first()

        assertEquals(WateringStats(totalXp = 420, streakDays = 3, lastSessionEpochDay = 20_000), stats)
    }

    @Test
    fun `après un jour manqué la série est à zéro mais l'XP reste`() = runTest {
        val stats = ObserveWateringStatsUseCase(preferences, { 20_002 * day + 1_000 }, { TimeZone.UTC })().first()

        assertEquals(0, stats.streakDays)
        assertEquals(420, stats.totalXp)
    }
}
