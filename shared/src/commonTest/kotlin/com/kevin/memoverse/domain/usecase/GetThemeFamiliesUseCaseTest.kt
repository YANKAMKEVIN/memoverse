package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.fake.FakeVerseBankRepository
import com.kevin.memoverse.fake.theme
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetThemeFamiliesUseCaseTest {

    @Test
    fun `regroupe les thèmes par famille dans l'ordre de la banque`() = runTest {
        val repository = FakeVerseBankRepository(
            themes = listOf(
                theme("foi", family = "Relation avec Dieu"),
                theme("paix", family = "Vie intérieure"),
                theme("priere", family = "Relation avec Dieu"),
            ),
        )

        val result = GetThemeFamiliesUseCase(repository)()

        assertIs<DataResult.Success<*>>(result)
        val families = (result as DataResult.Success).data
        assertEquals(listOf("Relation avec Dieu", "Vie intérieure"), families.map { it.name })
        assertEquals(listOf("foi", "priere"), families[0].themes.map { it.id })
    }

    @Test
    fun `propage l'erreur du repository`() = runTest {
        val error = IllegalStateException("boom")
        val result = GetThemeFamiliesUseCase(FakeVerseBankRepository(error = error))()

        assertEquals(DataResult.Error(error), result)
    }
}
