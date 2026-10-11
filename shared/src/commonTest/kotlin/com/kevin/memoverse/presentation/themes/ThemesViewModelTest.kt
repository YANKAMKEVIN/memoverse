package com.kevin.memoverse.presentation.themes

import app.cash.turbine.test
import com.kevin.memoverse.domain.usecase.GetThemeFamiliesUseCase
import com.kevin.memoverse.domain.usecase.ObserveLearningVersesUseCase
import com.kevin.memoverse.fake.FakeLearningRepository
import com.kevin.memoverse.fake.FakeVerseBankRepository
import com.kevin.memoverse.fake.learningVerse
import com.kevin.memoverse.fake.theme
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ThemesViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val learningRepository = FakeLearningRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repository: FakeVerseBankRepository) =
        ThemesViewModel(GetThemeFamiliesUseCase(repository), ObserveLearningVersesUseCase(learningRepository))

    @Test
    fun `affiche les familles de thèmes après chargement`() = runTest(dispatcher) {
        val repository = FakeVerseBankRepository(themes = listOf(theme("foi", verseIds = listOf("v1", "v2"))))

        viewModel(repository).state.test {
            assertTrue(awaitItem().isLoading)
            val loaded = awaitItem()
            assertEquals(false, loaded.isLoading)
            assertEquals(ThemeItemUi(id = "foi", name = "Foi", verseCount = 2), loaded.families.single().themes.single())
        }
    }

    @Test
    fun `compte les versets en cours de chaque thème en direct`() = runTest(dispatcher) {
        val repository = FakeVerseBankRepository(themes = listOf(theme("foi", verseIds = listOf("v1", "v2", "v3"))))

        viewModel(repository).state.test {
            skipItems(2)
            learningRepository.verses.value = listOf(learningVerse("v1"), learningVerse("v3"), learningVerse("ailleurs"))

            assertEquals(2, awaitItem().families.single().themes.single().learningCount)
        }
    }

    @Test
    fun `réessaie après une erreur`() = runTest(dispatcher) {
        val repository = FakeVerseBankRepository(themes = listOf(theme("foi")), error = IllegalStateException())
        val viewModel = viewModel(repository)

        viewModel.state.test {
            awaitItem()
            assertTrue(awaitItem().hasError)

            repository.error = null
            viewModel.onEvent(ThemesEvent.RetryClicked)

            assertTrue(awaitItem().isLoading)
            assertEquals(1, awaitItem().families.size)
        }
    }
}
