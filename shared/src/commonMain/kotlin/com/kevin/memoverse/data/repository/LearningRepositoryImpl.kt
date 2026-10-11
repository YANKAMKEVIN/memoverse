package com.kevin.memoverse.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kevin.memoverse.data.local.db.MemoverseDatabase
import com.kevin.memoverse.data.mapper.toDomain
import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.LearningVerse
import com.kevin.memoverse.domain.repository.LearningRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class LearningRepositoryImpl(
    private val database: MemoverseDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : LearningRepository {

    private val queries get() = database.learningVerseQueries

    override fun observeLearningVerses(): Flow<List<LearningVerse>> =
        queries.selectAll().asFlow().mapToList(ioDispatcher).map { entities -> entities.map { it.toDomain() } }

    override suspend fun addVerses(verses: List<LearningVerse>): DataResult<Unit> = write {
        queries.transaction {
            verses.forEach { queries.insertIfAbsent(it.verseId, it.themeId, it.stage.name, it.addedAtEpochMillis, it.dueAtEpochMillis) }
        }
    }

    override suspend fun updateVerse(verse: LearningVerse): DataResult<Unit> = write {
        queries.updateProgress(
            stage = verse.stage.name,
            successCount = verse.successCount.toLong(),
            successDays = verse.successDays.toLong(),
            lastSuccessEpochDay = verse.lastSuccessEpochDay,
            consecutiveFailures = verse.consecutiveFailures.toLong(),
            reviewStep = verse.reviewStep.toLong(),
            dueAtEpochMillis = verse.dueAtEpochMillis,
            verseId = verse.verseId,
        )
    }

    override suspend fun clear(): DataResult<Unit> = write { queries.deleteAll() }

    private suspend fun write(block: () -> Unit): DataResult<Unit> = try {
        withContext(ioDispatcher) { block() }
        DataResult.Success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DataResult.Error(e)
    }
}
