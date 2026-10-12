package com.kevin.memoverse.domain.review

import com.kevin.memoverse.fake.learningVerse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ReviewSchedulerTest {

    private val now = 1_000_000_000L
    private val hour = 3_600_000L
    private val day = 24 * hour

    @Test
    fun `chaque arrosage réussi espace le suivant de 6 h puis 1 puis 3 puis 7 jours`() {
        var verse = learningVerse("v1").copy(dueAtEpochMillis = now)
        val intervals = (1..4).map {
            verse = ReviewScheduler.apply(verse, isCorrect = true, nowEpochMillis = verse.dueAtEpochMillis)
            verse.dueAtEpochMillis
        }.zipWithNext { a, b -> b - a }

        assertEquals(listOf(day, 3 * day, 7 * day), intervals)
        assertEquals(4, verse.reviewStep)
    }

    @Test
    fun `une réussite sur un verset qui n'a pas soif ne change rien`() {
        val verse = learningVerse("v1").copy(reviewStep = 2, dueAtEpochMillis = now + hour)

        assertSame(verse, ReviewScheduler.apply(verse, isCorrect = true, nowEpochMillis = now))
    }

    @Test
    fun `un échec recule de 2 crans et fait revenir le verset dans 6 h`() {
        val verse = learningVerse("v1").copy(reviewStep = 3, dueAtEpochMillis = now)

        val after = ReviewScheduler.apply(verse, isCorrect = false, nowEpochMillis = now)

        assertEquals(1, after.reviewStep)
        assertEquals(now + 6 * hour, after.dueAtEpochMillis)
    }

    @Test
    fun `le dernier cran plafonne à 120 jours`() {
        val verse = learningVerse("v1").copy(reviewStep = 6, dueAtEpochMillis = now)

        val after = ReviewScheduler.apply(verse, isCorrect = true, nowEpochMillis = now)

        assertEquals(6, after.reviewStep)
        assertEquals(now + 120 * day, after.dueAtEpochMillis)
    }
}
