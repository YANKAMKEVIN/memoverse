package com.kevin.memoverse.presentation.profile

import com.kevin.memoverse.domain.usecase.ResetGardenUseCase
import com.kevin.memoverse.fake.FakeLearningRepository
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeLearningRepository(initial = listOf(learningVerse("v1")))
    private val viewModel get() = ProfileViewModel(ResetGardenUseCase(repository))

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `demande une confirmation avant d'effacer`() = runTest(dispatcher) {
        val vm = viewModel
        vm.onEvent(ProfileEvent.ResetClicked)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state.value.isConfirmingReset)
        assertEquals(1, repository.verses.value.size)
    }

    @Test
    fun `annuler ne touche pas au jardin`() = runTest(dispatcher) {
        val vm = viewModel
        vm.onEvent(ProfileEvent.ResetClicked)
        vm.onEvent(ProfileEvent.ResetCancelled)
        dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.state.value.isConfirmingReset)
        assertEquals(1, repository.verses.value.size)
    }

    @Test
    fun `confirmer vide le jardin`() = runTest(dispatcher) {
        val vm = viewModel
        vm.onEvent(ProfileEvent.ResetClicked)
        vm.onEvent(ProfileEvent.ResetConfirmed)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state.value.isResetDone)
        assertTrue(repository.verses.value.isEmpty())
    }
}
