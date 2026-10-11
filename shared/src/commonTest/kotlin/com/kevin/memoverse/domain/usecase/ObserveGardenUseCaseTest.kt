package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.GardenPlant
import com.kevin.memoverse.domain.model.MasteryStage
import com.kevin.memoverse.fake.FakeLearningRepository
import com.kevin.memoverse.fake.FakeVerseBankRepository
import com.kevin.memoverse.fake.learningVerse
import com.kevin.memoverse.fake.verse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveGardenUseCaseTest {

    @Test
    fun `associe chaque verset en apprentissage à sa référence`() = runTest {
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1"), learningVerse("v2", MasteryStage.ROOT)))
        val bank = FakeVerseBankRepository(verses = listOf(verse("v1"), verse("v2")))

        val garden = ObserveGardenUseCase(learning, bank, { NOW }, flowOf(Unit))().first()

        assertEquals(
            listOf(GardenPlant("v1", "Référence v1", MasteryStage.SEED, isThirsty = true), GardenPlant("v2", "Référence v2", MasteryStage.ROOT, isThirsty = true)),
            garden.plants,
        )
    }

    @Test
    fun `garde l'identifiant si la banque de versets est indisponible`() = runTest {
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1")))
        val bank = FakeVerseBankRepository(error = IllegalStateException())

        val garden = ObserveGardenUseCase(learning, bank, { NOW }, flowOf(Unit))().first()

        assertEquals("v1", garden.plants.single().reference)
    }

    @Test
    fun `donne le délai avant le prochain arrosage quand aucun verset n'a soif`() = runTest {
        val learning = FakeLearningRepository(
            initial = listOf(
                learningVerse("v1").copy(dueAtEpochMillis = NOW + 5_000),
                learningVerse("v2").copy(dueAtEpochMillis = NOW + 2_000),
            ),
        )
        val bank = FakeVerseBankRepository(verses = listOf(verse("v1"), verse("v2")))

        val garden = ObserveGardenUseCase(learning, bank, { NOW }, flowOf(Unit))().first()

        assertEquals(0, garden.thirstyCount)
        assertEquals(2_000L, garden.millisUntilNextWatering)
    }

    @Test
    fun `un verset qui arrive à échéance app ouverte se met à avoir soif`() = runTest {
        // Régression QA : le jardin restait « arrosé » tant que la base ne changeait pas.
        var now = NOW
        val ticks = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1").copy(dueAtEpochMillis = NOW + 50_000)))
        val bank = FakeVerseBankRepository(verses = listOf(verse("v1")))

        ObserveGardenUseCase(learning, bank, { now }, ticks)().test {
            assertEquals(0, awaitItem().thirstyCount)
            now += 60_000
            ticks.emit(Unit)
            assertEquals(1, awaitItem().thirstyCount)
        }
    }

    private companion object {
        const val NOW = 1_000_000L
    }
}
