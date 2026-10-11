package com.kevin.memoverse.data.system

import com.kevin.memoverse.domain.repository.TimeProvider
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SystemTimeProvider : TimeProvider {
    @OptIn(ExperimentalTime::class)
    override fun nowEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()
}
