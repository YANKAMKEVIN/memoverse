package com.kevin.memoverse.domain.usecase

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Jour calendaire de l'utilisateur (et non le jour UTC) : les séries suivent son minuit. */
@OptIn(ExperimentalTime::class)
internal fun localEpochDay(epochMillis: Long, timeZone: TimeZone): Long =
    Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone).date.toEpochDays()
