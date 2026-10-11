package com.kevin.memoverse.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kevin.memoverse.data.local.db.MemoverseDatabase
import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.LearningVerse
import com.kevin.memoverse.domain.model.MasteryStage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LearningRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()

    private fun repository(): LearningRepositoryImpl {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MemoverseDatabase.Schema.create(driver)
        return LearningRepositoryImpl(MemoverseDatabase(driver), dispatcher)
    }

    @Test
    fun `enregistre puis relit les versets en apprentissage`() = runTest(dispatcher) {
        val repository = repository()
        val verses = listOf(
            LearningVerse("v1", "foi", MasteryStage.SEED, 10L),
            LearningVerse("v2", "foi", MasteryStage.SEED, 20L),
        )

        assertIs<DataResult.Success<Unit>>(repository.addVerses(verses))

        assertEquals(verses, repository.observeLearningVerses().first())
    }

    @Test
    fun `n'écrase pas un verset déjà en apprentissage`() = runTest(dispatcher) {
        val repository = repository()
        val original = LearningVerse("v1", "foi", MasteryStage.ROOT, 10L)
        repository.addVerses(listOf(original))

        repository.addVerses(listOf(LearningVerse("v1", "paix", MasteryStage.SEED, 99L)))

        assertEquals(listOf(original), repository.observeLearningVerses().first())
    }

    @Test
    fun `enregistre la progression d'un verset`() = runTest(dispatcher) {
        val repository = repository()
        repository.addVerses(listOf(LearningVerse("v1", "foi", MasteryStage.SEED, 10L)))
        val grown = LearningVerse("v1", "foi", MasteryStage.ROOT, 10L, successCount = 1, successDays = 1, lastSuccessEpochDay = 20_000L, consecutiveFailures = 0, reviewStep = 2, dueAtEpochMillis = 99L)

        assertIs<DataResult.Success<Unit>>(repository.updateVerse(grown))

        assertEquals(listOf(grown), repository.observeLearningVerses().first())
    }
}
