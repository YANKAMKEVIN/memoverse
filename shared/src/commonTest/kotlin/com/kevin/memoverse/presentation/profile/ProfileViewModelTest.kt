package com.kevin.memoverse.presentation.profile

import com.kevin.memoverse.domain.model.TypefaceChoice
import com.kevin.memoverse.domain.usecase.ObserveTypefaceUseCase
import com.kevin.memoverse.domain.usecase.ResetGardenUseCase
import com.kevin.memoverse.domain.usecase.SelectTypefaceUseCase
import com.kevin.memoverse.fake.FakePreferencesRepository
import com.kevin.memoverse.presentation.common.TypefaceUi
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
    private val preferences = FakePreferencesRepository()
    private val viewModel get() = ProfileViewModel(ResetGardenUseCase(repository), ObserveTypefaceUseCase(preferences), SelectTypefaceUseCase(preferences))

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

    @Test
    fun `propose les cinq polices avec celle d'origine cochée`() = runTest(dispatcher) {
        val vm = viewModel
        dispatcher.scheduler.advanceUntilIdle()

        val typefaces = vm.state.value.typefaces
        assertEquals(TypefaceUi.entries, typefaces.map { it.typeface })
        assertEquals(listOf(TypefaceUi.JARDIN), typefaces.filter { it.isSelected }.map { it.typeface })
    }

    @Test
    fun `choisir une police l'enregistre et la coche`() = runTest(dispatcher) {
        val vm = viewModel
        vm.onEvent(ProfileEvent.TypefaceSelected(TypefaceUi.CORMORANT))
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(TypefaceChoice.CORMORANT, preferences.typeface.value)
        assertEquals(TypefaceUi.CORMORANT, vm.state.value.typefaces.single { it.isSelected }.typeface)
    }

    @Test
    fun `signale un échec d'enregistrement de la police`() = runTest(dispatcher) {
        preferences.saveError = IllegalStateException("disque plein")
        val vm = viewModel
        vm.onEvent(ProfileEvent.TypefaceSelected(TypefaceUi.GLOOCK))
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state.value.hasTypefaceSaveFailed)
        assertEquals(TypefaceUi.JARDIN, vm.state.value.typefaces.single { it.isSelected }.typeface)
    }

    @Test
    fun `la confirmation de réinitialisation garde la liste des polices`() = runTest(dispatcher) {
        val vm = viewModel
        vm.onEvent(ProfileEvent.ResetClicked)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(5, vm.state.value.typefaces.size)
    }
}
