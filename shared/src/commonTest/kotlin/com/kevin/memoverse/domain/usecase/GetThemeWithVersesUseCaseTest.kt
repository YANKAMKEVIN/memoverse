package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.fake.FakeVerseBankRepository
import com.kevin.memoverse.fake.theme
import com.kevin.memoverse.fake.verse
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetThemeWithVersesUseCaseTest {

    private val repository = FakeVerseBankRepository(
        themes = listOf(theme("foi", verseIds = listOf("v2", "v1"))),
        verses = listOf(verse("v1"), verse("v2"), verse("v3")),
    )

    @Test
    fun `retourne le thème avec ses versets dans l'ordre du thème`() = runTest {
        val result = GetThemeWithVersesUseCase(repository)("foi")

        assertIs<DataResult.Success<*>>(result)
        val themeWithVerses = (result as DataResult.Success).data
        assertEquals("foi", themeWithVerses.theme.id)
        assertEquals(listOf("v2", "v1"), themeWithVerses.verses.map { it.id })
    }

    @Test
    fun `retourne une erreur si le thème est inconnu`() = runTest {
        val result = GetThemeWithVersesUseCase(repository)("inconnu")

        assertIs<DataResult.Error>(result)
        assertIs<NoSuchElementException>(result.cause)
    }
}
