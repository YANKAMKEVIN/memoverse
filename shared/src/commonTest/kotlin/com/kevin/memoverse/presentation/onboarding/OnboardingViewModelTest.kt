package com.kevin.memoverse.presentation.onboarding

import com.kevin.memoverse.domain.usecase.ShouldShowOnboardingUseCase
import com.kevin.memoverse.fake.FakeLearningRepository
import com.kevin.memoverse.fake.FakePreferencesRepository
import com.kevin.memoverse.fake.learningVerse
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val preferences = FakePreferencesRepository()

    private fun viewModel(learning: FakeLearningRepository = FakeLearningRepository()) =
        OnboardingViewModel(ShouldShowOnboardingUseCase(preferences, learning), preferences)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `au premier lancement l'accueil s'affiche`() = runTest(dispatcher) {
        val vm = viewModel()
        assertNull(vm.showOnboarding.value)

        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, vm.showOnboarding.value)
    }

    @Test
    fun `terminer l'accueil le retient pour les lancements suivants`() = runTest(dispatcher) {
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.onFinished()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, vm.showOnboarding.value)
        assertTrue(preferences.onboardingSeen)
        val next = viewModel()
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(false, next.showOnboarding.value)
    }

    @Test
    fun `une personne qui a déjà des versets ne voit pas l'accueil`() = runTest(dispatcher) {
        val vm = viewModel(FakeLearningRepository(initial = listOf(learningVerse("v1"))))
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, vm.showOnboarding.value)
        assertTrue(preferences.onboardingSeen)
    }
}
