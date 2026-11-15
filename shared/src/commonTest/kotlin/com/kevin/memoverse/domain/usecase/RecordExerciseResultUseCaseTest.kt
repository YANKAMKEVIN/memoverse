package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.MasteryStage
import com.kevin.memoverse.fake.FakeLearningRepository
import com.kevin.memoverse.fake.learningVerse
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecordExerciseResultUseCaseTest {

    @Test
    fun `enregistre la croissance du verset`() = runTest {
        val start = learningVerse("v1").copy(successCount = 2)
        val repository = FakeLearningRepository(initial = listOf(start))

        val outcome = (RecordExerciseResultUseCase(repository, { 20_000L * 86_400_000L })(start, isCorrect = true) as DataResult.Success).data

        assertTrue(outcome.hasGrown)
        assertEquals(MasteryStage.ROOT, repository.verses.value.single().stage)
    }

    @Test
    fun `deux réussites la même nuit à Paris comptent pour un seul jour`() = runTest {
        // 00h30 puis 01h30 à Paris le 15 janvier 2026 : même jour local, mais deux jours UTC
        // (2026-01-14T23:30Z et 2026-01-15T00:30Z).
        val times = ArrayDeque(listOf(1_768_433_400_000L, 1_768_437_000_000L))
        val start = learningVerse("v1", stage = MasteryStage.ROOT).copy(successCount = 2)
        val repository = FakeLearningRepository(initial = listOf(start))
        val record = RecordExerciseResultUseCase(repository, { times.removeFirst() }, { TimeZone.of("Europe/Paris") })

        record(repository.verses.value.single(), isCorrect = true)
        record(repository.verses.value.single(), isCorrect = true)

        val verse = repository.verses.value.single()
        assertEquals(MasteryStage.ROOT, verse.stage, "4 réussites sur un seul jour ne suffisent pas pour l'arbre")
        assertEquals(1, verse.successDays)
    }
}
