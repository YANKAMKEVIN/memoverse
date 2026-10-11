package com.kevin.memoverse.domain.notification

import com.kevin.memoverse.domain.model.HiddenPosition
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Règles du cadrage : au plus 2 rappels à l'avance, espacés d'au moins 3 h, jamais entre 21 h
 * et 8 h (heure locale). Un verset qui a déjà soif est rappelé au plus tôt dans 1 h, pour ne pas
 * notifier quelqu'un qui vient de quitter l'app.
 */
object ReviewNotificationPlanner {
    const val MAX_NOTIFICATIONS = 2
    const val MIN_DELAY_MILLIS = 3_600_000L
    const val MIN_GAP_MILLIS = 3 * 3_600_000L
    val QUIET_END: LocalTime = LocalTime(8, 0)
    val QUIET_START: LocalTime = LocalTime(21, 0)

    data class DueVerse(val reference: String, val dueAtEpochMillis: Long)

    @OptIn(ExperimentalTime::class)
    fun plan(verses: List<DueVerse>, nowEpochMillis: Long, timeZone: TimeZone): List<PlannedNotification> {
        val planned = mutableListOf<PlannedNotification>()
        var earliestAllowed = nowEpochMillis + MIN_DELAY_MILLIS
        val pending = verses.sortedBy { it.dueAtEpochMillis }.toMutableList()
        while (pending.isNotEmpty() && planned.size < MAX_NOTIFICATIONS) {
            val at = outsideQuietHours(maxOf(pending.first().dueAtEpochMillis, earliestAllowed), timeZone)
            // Tous les versets qui ont soif à ce moment-là partagent le même rappel.
            val group = pending.filter { it.dueAtEpochMillis <= at }
            pending.removeAll(group)
            val part = HiddenPosition.entries[(at / MIN_DELAY_MILLIS % HiddenPosition.entries.size).toInt()]
            planned += PlannedNotification(at, group.map { it.reference }, part)
            earliestAllowed = at + MIN_GAP_MILLIS
        }
        return planned
    }

    @OptIn(ExperimentalTime::class)
    private fun outsideQuietHours(epochMillis: Long, timeZone: TimeZone): Long {
        val local = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone)
        val date = when {
            local.time < QUIET_END -> local.date
            local.time >= QUIET_START -> local.date.plus(1, DateTimeUnit.DAY)
            else -> return epochMillis
        }
        return date.atTime(QUIET_END).toInstant(timeZone).toEpochMilliseconds()
    }
}
