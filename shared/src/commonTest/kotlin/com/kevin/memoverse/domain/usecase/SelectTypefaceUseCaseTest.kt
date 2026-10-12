package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.TypefaceChoice
import com.kevin.memoverse.fake.FakePreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SelectTypefaceUseCaseTest {

    @Test
    fun `la police d'origine est utilisée tant que rien n'est choisi`() = runTest {
        assertEquals(TypefaceChoice.JARDIN, ObserveTypefaceUseCase(FakePreferencesRepository()).invoke().first())
    }

    @Test
    fun `la police choisie est enregistrée et observée`() = runTest {
        val repository = FakePreferencesRepository()

        assertIs<DataResult.Success<Unit>>(SelectTypefaceUseCase(repository)(TypefaceChoice.FRAUNCES))

        assertEquals(TypefaceChoice.FRAUNCES, ObserveTypefaceUseCase(repository)().first())
    }

    @Test
    fun `remonte l'échec d'enregistrement`() = runTest {
        val repository = FakePreferencesRepository(saveError = IllegalStateException("disque plein"))

        assertIs<DataResult.Error>(SelectTypefaceUseCase(repository)(TypefaceChoice.GLOOCK))
        assertEquals(TypefaceChoice.JARDIN, repository.typeface.value)
    }
}
