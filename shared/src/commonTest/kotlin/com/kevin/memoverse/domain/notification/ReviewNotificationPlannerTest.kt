package com.kevin.memoverse.domain.notification

import com.kevin.memoverse.domain.notification.ReviewNotificationPlanner.DueVerse
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ReviewNotificationPlannerTest {

    private val zone = TimeZone.UTC
    private fun at(day: Int, hour: Int, minute: Int = 0) =
        LocalDateTime(2026, 10, day, hour, minute).toInstant(zone).toEpochMilliseconds()

    @Test
    fun `un verset qui a déjà soif est rappelé une heure plus tard`() {
        val plan = ReviewNotificationPlanner.plan(listOf(DueVerse("Hébreux 11:1", at(11, 9))), at(11, 14), zone)

        assertEquals(listOf(at(11, 15)), plan.map { it.atEpochMillis })
        assertEquals(listOf("Hébreux 11:1"), plan.single().references)
    }

    @Test
    fun `aucun rappel entre 21 h et 8 h`() {
        val evening = ReviewNotificationPlanner.plan(listOf(DueVerse("A", at(11, 22))), at(11, 10), zone)
        val night = ReviewNotificationPlanner.plan(listOf(DueVerse("A", at(12, 3))), at(11, 10), zone)

        assertEquals(at(12, 8), evening.single().atEpochMillis)
        assertEquals(at(12, 8), night.single().atEpochMillis)
    }

    @Test
    fun `les versets qui ont soif au même moment partagent un rappel`() {
        val plan = ReviewNotificationPlanner.plan(listOf(DueVerse("A", at(12, 9)), DueVerse("B", at(12, 9))), at(11, 10), zone)

        assertEquals(listOf(listOf("A", "B")), plan.map { it.references })
    }

    @Test
    fun `au plus 2 rappels, espacés d'au moins 3 h`() {
        val verses = listOf(DueVerse("A", at(12, 9)), DueVerse("B", at(12, 10)), DueVerse("C", at(12, 18)), DueVerse("D", at(13, 9)))

        val plan = ReviewNotificationPlanner.plan(verses, at(11, 10), zone)

        assertEquals(2, plan.size)
        assertEquals(listOf(at(12, 9), at(12, 12)), plan.map { it.atEpochMillis })
        assertEquals(listOf("B"), plan[1].references)
        assertTrue(plan.zipWithNext().all { (a, b) -> b.atEpochMillis - a.atEpochMillis >= ReviewNotificationPlanner.MIN_GAP_MILLIS })
    }

    @Test
    fun `aucun rappel pour un jardin vide`() {
        assertEquals(emptyList(), ReviewNotificationPlanner.plan(emptyList(), at(11, 10), zone))
    }
}
