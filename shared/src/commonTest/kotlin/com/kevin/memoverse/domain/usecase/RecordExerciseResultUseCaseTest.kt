package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.MasteryStage
import com.kevin.memoverse.fake.FakeLearningRepository
import com.kevin.memoverse.fake.learningVerse
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecordExerciseResultUseCaseTest {

    @Test
    fun `enregistre la croissance du verset`() = runTest {
        val start = learningVerse("v1").copy(successCount = 2)
        val repository = FakeLearningRepository(initial = listOf(start))

        val outcome = (RecordExerciseResultUseCase(repository) { 20_000L * 86_400_000L }(start, isCorrect = true) as DataResult.Success).data

        assertTrue(outcome.hasGrown)
        assertEquals(MasteryStage.ROOT, repository.verses.value.single().stage)
    }
}
