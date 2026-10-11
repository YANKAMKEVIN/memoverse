package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.LearningVerse
import com.kevin.memoverse.domain.model.MasteryStage
import com.kevin.memoverse.domain.model.StartLearningResult
import com.kevin.memoverse.fake.FakeLearningRepository
import com.kevin.memoverse.fake.learningVerse
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class StartLearningUseCaseTest {

    private val now = 1_700_000_000_000L

    private fun useCase(repository: FakeLearningRepository) = StartLearningUseCase(repository) { now }

    @Test
    fun `enregistre les versets choisis comme graines`() = runTest {
        val repository = FakeLearningRepository()

        val result = useCase(repository)("foi", setOf("v1", "v2", "v3"))

        assertEquals(StartLearningResult.Started(addedCount = 3), result)
        assertEquals(
            listOf("v1", "v2", "v3").map { LearningVerse(it, "foi", MasteryStage.SEED, now, dueAtEpochMillis = now) },
            repository.verses.value,
        )
    }

    @Test
    fun `refuse une sélection de moins de 3 versets`() = runTest {
        val result = useCase(FakeLearningRepository())("foi", setOf("v1", "v2"))

        assertEquals(StartLearningResult.InvalidSelection, result)
    }

    @Test
    fun `refuse de dépasser 5 versets actifs`() = runTest {
        val repository = FakeLearningRepository(initial = listOf("a", "b", "c").map { learningVerse(it) })

        val result = useCase(repository)("paix", setOf("v1", "v2", "v3"))

        assertEquals(StartLearningResult.TooManyActiveVerses(activeCount = 3, maxActive = 5), result)
        assertEquals(3, repository.verses.value.size)
    }

    @Test
    fun `ne compte pas les versets déjà au stade arbre ou fruit`() = runTest {
        val repository = FakeLearningRepository(
            initial = listOf(
                learningVerse("a", MasteryStage.TREE),
                learningVerse("b", MasteryStage.FRUIT),
                learningVerse("c", MasteryStage.ROOT),
            ),
        )

        val result = useCase(repository)("paix", setOf("v1", "v2", "v3", "v4"))

        assertEquals(StartLearningResult.Started(addedCount = 4), result)
    }

    @Test
    fun `retourne un échec si l'enregistrement échoue`() = runTest {
        val repository = FakeLearningRepository(saveError = IllegalStateException("disque plein"))

        val result = useCase(repository)("foi", setOf("v1", "v2", "v3"))

        assertIs<StartLearningResult.Failed>(result)
    }
}
