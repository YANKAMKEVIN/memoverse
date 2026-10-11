package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.fake.FakeLearningRepository
import com.kevin.memoverse.fake.learningVerse
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ResetGardenUseCaseTest {

    @Test
    fun `vide le jardin`() = runTest {
        val repository = FakeLearningRepository(initial = listOf(learningVerse("v1"), learningVerse("v2")))

        assertIs<DataResult.Success<Unit>>(ResetGardenUseCase(repository)())

        assertTrue(repository.verses.value.isEmpty())
    }
}
