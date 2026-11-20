package com.kevin.memoverse.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class WateringStatsTest {

    @Test
    fun `la première séance ouvre une série d'un jour et compte son XP`() {
        assertEquals(WateringStats(totalXp = 60, streakDays = 1, lastSessionEpochDay = 100), WateringStats().afterSession(60, today = 100))
    }

    @Test
    fun `une séance le lendemain prolonge la série`() {
        val stats = WateringStats(totalXp = 60, streakDays = 3, lastSessionEpochDay = 100).afterSession(40, today = 101)

        assertEquals(WateringStats(totalXp = 100, streakDays = 4, lastSessionEpochDay = 101), stats)
    }

    @Test
    fun `une deuxième séance le même jour ajoute l'XP sans allonger la série`() {
        val stats = WateringStats(totalXp = 60, streakDays = 3, lastSessionEpochDay = 100).afterSession(10, today = 100)

        assertEquals(3, stats.streakDays)
        assertEquals(70, stats.totalXp)
    }

    @Test
    fun `un jour manqué fait repartir la série à un`() {
        assertEquals(1, WateringStats(totalXp = 60, streakDays = 5, lastSessionEpochDay = 100).afterSession(0, today = 102).streakDays)
    }

    @Test
    fun `la série se lit encore le lendemain mais plus après un jour manqué`() {
        val stats = WateringStats(totalXp = 60, streakDays = 5, lastSessionEpochDay = 100)

        assertEquals(5, stats.currentStreak(today = 100))
        assertEquals(5, stats.currentStreak(today = 101))
        assertEquals(0, stats.currentStreak(today = 102))
        assertEquals(0, WateringStats().currentStreak(today = 100))
    }
}
