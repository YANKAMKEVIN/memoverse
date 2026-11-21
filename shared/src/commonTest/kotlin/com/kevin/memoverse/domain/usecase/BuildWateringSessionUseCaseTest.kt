package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.exercise.ExerciseFactory
import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.MasteryStage
import com.kevin.memoverse.fake.FakeLearningRepository
import com.kevin.memoverse.fake.FakeVerseBankRepository
import com.kevin.memoverse.fake.learningVerse
import com.kevin.memoverse.fake.verse
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildWateringSessionUseCaseTest {

    private val bank = FakeVerseBankRepository(
        verses = (1..8).map { verse("v$it", text = "mot$it alpha beta gamma delta epsilon zeta eta theta iota kappa lambda") },
    )

    private fun useCase(learning: FakeLearningRepository) = BuildWateringSessionUseCase(learning, bank, ExerciseFactory(Random(1))) { NOW }

    @Test
    fun `prévoit 2 exercices par verset en croissance - entrelacés`() = runTest {
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1"), learningVerse("v2")))

        val session = (useCase(learning)() as DataResult.Success).data

        assertEquals(listOf("v1", "v2", "v1", "v2"), session.exercises.map { it.verse.id })
    }

    @Test
    fun `alterne les deux tours d'exercices sans repeter un verset de suite`() = runTest {
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1"), learningVerse("v2"), learningVerse("v3")))

        val session = (useCase(learning)() as DataResult.Success).data

        assertEquals(listOf("v1", "v2", "v1", "v3", "v2", "v3"), session.exercises.map { it.verse.id })
    }

    @Test
    fun `n'arrose que les versets qui ont soif`() = runTest {
        val learning = FakeLearningRepository(
            initial = listOf(learningVerse("v1").copy(dueAtEpochMillis = NOW + 1), learningVerse("v2", MasteryStage.FRUIT)),
        )

        val session = (useCase(learning)() as DataResult.Success).data

        assertTrue(session.exercises.all { it.verse.id == "v2" })
    }

    @Test
    fun `une séance vide si rien n'est planté`() = runTest {
        val session = (useCase(FakeLearningRepository())() as DataResult.Success).data

        assertTrue(session.exercises.isEmpty())
    }

    private companion object {
        const val NOW = 1_000_000L
    }
}
