package com.kevin.memoverse.domain.repository

fun interface TimeProvider {
    fun nowEpochMillis(): Long
}
