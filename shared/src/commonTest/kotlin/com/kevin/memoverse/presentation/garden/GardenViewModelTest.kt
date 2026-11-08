package com.kevin.memoverse.presentation.garden

import app.cash.turbine.test
import com.kevin.memoverse.domain.model.MasteryStage
import com.kevin.memoverse.domain.usecase.ObserveGardenUseCase
import com.kevin.memoverse.fake.FakeLearningRepository
import com.kevin.memoverse.fake.FakeVerseBankRepository
import com.kevin.memoverse.fake.learningVerse
import com.kevin.memoverse.fake.verse
import com.kevin.memoverse.presentation.common.StageUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GardenViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val learning = FakeLearningRepository()
    private val bank = FakeVerseBankRepository(verses = listOf(verse("v1"), verse("v2"), verse("v3")))

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `un jardin vide invite à planter`() = runTest(dispatcher) {
        GardenViewModel(ObserveGardenUseCase(learning, bank, { 0L }, flowOf(Unit))).state.test {
            assertTrue(awaitItem().isLoading)
            val loaded = awaitItem()
            assertFalse(loaded.hasPlants)
            assertEquals(5, loaded.maxActive)
        }
    }

    @Test
    fun `une erreur de lecture n'est pas un jardin vide - et se reessaie`() = runTest(dispatcher) {
        var shouldFail = true
        val ticks = flow { if (shouldFail) error("base illisible") else emit(Unit) }
        val viewModel = GardenViewModel(ObserveGardenUseCase(learning, bank, { 0L }, ticks))
        viewModel.state.test {
            skipItems(1)
            assertTrue(awaitItem().hasError)

            shouldFail = false
            viewModel.retry()
            assertFalse(awaitItem().hasError)
        }
    }

    @Test
    fun `affiche les versets plantés et compte les actifs`() = runTest(dispatcher) {
        val viewModel = GardenViewModel(ObserveGardenUseCase(learning, bank, { 0L }, flowOf(Unit)))
        viewModel.state.test {
            skipItems(2)
            learning.verses.value = listOf(learningVerse("v1"), learningVerse("v2", MasteryStage.ROOT), learningVerse("v3", MasteryStage.TREE))

            val state = awaitItem()
            assertEquals(listOf("Référence v1", "Référence v2", "Référence v3"), state.plants.map { it.reference })
            assertEquals(StageUi.TREE, state.plants.last().stage)
            assertEquals(2, state.activeCount)
        }
    }
}
