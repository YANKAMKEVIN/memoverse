package com.kevin.memoverse.domain.repository

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.LearningVerse
import kotlinx.coroutines.flow.Flow

/** Progression de l'utilisateur, stockée sur l'appareil. */
interface LearningRepository {
    fun observeLearningVerses(): Flow<List<LearningVerse>>
    suspend fun addVerses(verses: List<LearningVerse>): DataResult<Unit>
    suspend fun updateVerse(verse: LearningVerse): DataResult<Unit>
    suspend fun clear(): DataResult<Unit>
}
