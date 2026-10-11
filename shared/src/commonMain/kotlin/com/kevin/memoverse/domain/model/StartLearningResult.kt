package com.kevin.memoverse.domain.model

sealed interface StartLearningResult {
    data class Started(val addedCount: Int) : StartLearningResult
    data object InvalidSelection : StartLearningResult
    data class TooManyActiveVerses(val activeCount: Int, val maxActive: Int) : StartLearningResult
    data class Failed(val cause: Throwable) : StartLearningResult
}
