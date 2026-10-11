package com.kevin.memoverse.presentation.themedetail

import app.cash.turbine.test
import com.kevin.memoverse.domain.usecase.GetThemeWithVersesUseCase
import com.kevin.memoverse.domain.usecase.ObserveLearningVersesUseCase
import com.kevin.memoverse.domain.usecase.StartLearningUseCase
import com.kevin.memoverse.domain.usecase.ToggleVerseSelectionUseCase
import com.kevin.memoverse.fake.FakeLearningRepository
import com.kevin.memoverse.fake.FakeVerseBankRepository
import com.kevin.memoverse.fake.learningVerse
import com.kevin.memoverse.fake.theme
import com.kevin.memoverse.fake.verse
import kotlinx.coroutines.Dispatchers
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
class ThemeDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val verseIds = listOf("v1", "v2", "v3", "v4", "v5")
    private val verseBank = FakeVerseBankRepository(
        themes = listOf(theme("foi", verseIds = verseIds)),
        verses = verseIds.map { verse(it) },
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(learning: FakeLearningRepository = FakeLearningRepository()) = ThemeDetailViewModel(
        themeId = "foi",
        getThemeWithVerses = GetThemeWithVersesUseCase(verseBank),
        toggleVerseSelection = ToggleVerseSelectionUseCase(),
        observeLearningVerses = ObserveLearningVersesUseCase(learning),
        startLearning = StartLearningUseCase(learning) { 0L },
    )

    @Test
    fun `charge le thème et ses versets sans sélection`() = runTest(dispatcher) {
        viewModel().state.test {
            assertTrue(awaitItem().isLoading)
            val loaded = awaitItem()
            assertEquals("Foi", loaded.themeName)
            assertEquals(verseIds, loaded.verses.map { it.id })
            assertTrue(loaded.verses.none { it.isSelected || it.isLearning })
            assertFalse(loaded.canStart)
        }
    }

    @Test
    fun `autorise à commencer à partir de 3 versets cochés`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.state.test {
            skipItems(2)
            listOf("v1", "v2", "v3").forEach { viewModel.onEvent(ThemeDetailEvent.VerseToggled(it)) }
            dispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertEquals(3, state.selectedCount)
            assertTrue(state.canStart)
        }
    }

    @Test
    fun `signale la limite au cinquième verset`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.state.test {
            skipItems(2)
            verseIds.forEach { viewModel.onEvent(ThemeDetailEvent.VerseToggled(it)) }
            dispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertEquals(4, state.selectedCount)
            assertTrue(state.isLimitReached)
            assertFalse(state.verses.single { it.id == "v5" }.isSelected)
        }
    }

    @Test
    fun `verrouille les versets déjà en apprentissage`() = runTest(dispatcher) {
        val viewModel = viewModel(FakeLearningRepository(initial = listOf(learningVerse("v2"))))
        viewModel.state.test {
            dispatcher.scheduler.advanceUntilIdle()
            viewModel.onEvent(ThemeDetailEvent.VerseToggled("v2"))
            dispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state.verses.single { it.id == "v2" }.isLearning)
            assertEquals(0, state.selectedCount)
        }
    }

    @Test
    fun `enregistre la sélection et signale le démarrage`() = runTest(dispatcher) {
        val learning = FakeLearningRepository()
        val viewModel = viewModel(learning)
        viewModel.state.test {
            skipItems(2)
            listOf("v1", "v2", "v3").forEach { viewModel.onEvent(ThemeDetailEvent.VerseToggled(it)) }

            viewModel.onEvent(ThemeDetailEvent.StartClicked)
            dispatcher.scheduler.advanceUntilIdle()

            assertTrue(expectMostRecentItem().isStarted)
            assertEquals(setOf("v1", "v2", "v3"), learning.verses.value.map { it.verseId }.toSet())
        }
    }

    @Test
    fun `affiche une erreur si le plafond de versets actifs est atteint`() = runTest(dispatcher) {
        val learning = FakeLearningRepository(initial = listOf("a", "b", "c").map { learningVerse(it, themeId = "paix") })
        val viewModel = viewModel(learning)
        viewModel.state.test {
            skipItems(2)
            listOf("v1", "v2", "v3").forEach { viewModel.onEvent(ThemeDetailEvent.VerseToggled(it)) }

            viewModel.onEvent(ThemeDetailEvent.StartClicked)
            dispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertFalse(state.isStarted)
            assertEquals(StartErrorUi.TooManyActiveVerses(activeCount = 3, maxActive = 5), state.startError)
        }
    }

    @Test
    fun `un jardin plein est annoncé dès l'ouverture du thème`() = runTest(dispatcher) {
        // Régression QA : le plafond n'était signalé qu'après avoir coché et touché « Planter ».
        val learning = FakeLearningRepository(initial = listOf("a", "b", "c").map { learningVerse(it, themeId = "paix") })
        viewModel(learning).state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state.isGardenFull)
            assertEquals(2, state.freeSlots)
            assertEquals(3, state.activeCount)
        }
    }

    @Test
    fun `planter n'est possible que si la sélection tient dans la place restante`() = runTest(dispatcher) {
        val learning = FakeLearningRepository(initial = listOf("a", "b").map { learningVerse(it, themeId = "paix") })
        val viewModel = viewModel(learning)
        viewModel.state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()
            listOf("v1", "v2", "v3", "v4").forEach { viewModel.onEvent(ThemeDetailEvent.VerseToggled(it)) }
            dispatcher.scheduler.advanceUntilIdle()
            assertFalse(expectMostRecentItem().canStart)

            viewModel.onEvent(ThemeDetailEvent.VerseToggled("v4"))
            dispatcher.scheduler.advanceUntilIdle()
            assertTrue(expectMostRecentItem().canStart)
        }
    }
}
