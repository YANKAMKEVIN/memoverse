package com.kevin.memoverse.presentation.progression

import app.cash.turbine.test
import com.kevin.memoverse.domain.usecase.ObserveGardenUseCase
import com.kevin.memoverse.domain.usecase.ObserveWateringStatsUseCase
import com.kevin.memoverse.fake.FakeLearningRepository
import com.kevin.memoverse.fake.FakePreferencesRepository
import com.kevin.memoverse.fake.FakeVerseBankRepository
import com.kevin.memoverse.fake.learningVerse
import com.kevin.memoverse.fake.verse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class ProgressionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val learning = FakeLearningRepository()
    private val bank = FakeVerseBankRepository(verses = listOf(verse("v1"), verse("v2")))

    private fun viewModel() = ProgressionViewModel(
        ObserveGardenUseCase(learning, bank, { 0L }, flowOf(Unit)),
        ObserveWateringStatsUseCase(FakePreferencesRepository(), { 0L }),
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `une erreur de lecture affiche une erreur et non une progression vide`() = runTest(dispatcher) {
        learning.observeError = IllegalStateException("base illisible")
        viewModel().state.test {
            assertTrue(awaitItem().isLoading)
            val failed = awaitItem()
            assertTrue(failed.hasError)
            assertFalse(failed.isLoading)
        }
    }

    @Test
    fun `réessayer après une erreur recharge la progression`() = runTest(dispatcher) {
        learning.observeError = IllegalStateException("base illisible")
        learning.verses.value = listOf(learningVerse("v1"))
        val viewModel = viewModel()
        viewModel.state.test {
            skipItems(1)
            assertTrue(awaitItem().hasError)

            learning.observeError = null
            viewModel.onRetry()

            assertTrue(awaitItem().isLoading)
            val loaded = awaitItem()
            assertFalse(loaded.hasError)
            assertEquals(1, loaded.totalCount)
        }
    }
}
