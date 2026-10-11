package com.kevin.memoverse.data.repository

import com.kevin.memoverse.data.local.VerseBankDataSource
import com.kevin.memoverse.domain.model.DataResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class VerseBankRepositoryImplTest {

    private class FakeDataSource(private val json: String) : VerseBankDataSource {
        var readCount = 0
        override suspend fun readVerseBankJson(): String {
            readCount++
            return json
        }
    }

    private val bankJson = """
        {
          "translation": "Louis Segond 1910",
          "source": "test",
          "themes": [{"id": "foi", "name": "Foi", "family": "Relation avec Dieu", "verseIds": ["v1", "v2"]}],
          "verses": [
            {"id": "v1", "reference": "Hébreux 11:1", "book": "Hébreux", "chapter": 11, "verseStart": 1, "verseEnd": 1,
             "text": "Or la foi…", "context": "c", "wordCount": 19, "difficulty": "facile", "themes": ["foi"]},
            {"id": "v2", "reference": "Romains 10:17", "book": "Romains", "chapter": 10, "verseStart": 17, "verseEnd": 17,
             "text": "Ainsi la foi…", "context": "c", "wordCount": 18, "difficulty": "facile", "themes": ["foi"]}
          ]
        }
    """.trimIndent()

    @Test
    fun `lit la banque une seule fois puis sert le cache`() = runTest {
        val dataSource = FakeDataSource(bankJson)
        val repository = VerseBankRepositoryImpl(dataSource)

        repository.getThemes()
        repository.getVerses(listOf("v1"))

        assertEquals(1, dataSource.readCount)
    }

    @Test
    fun `retourne les versets demandés dans l'ordre et ignore les inconnus`() = runTest {
        val repository = VerseBankRepositoryImpl(FakeDataSource(bankJson))

        val result = repository.getVerses(listOf("v2", "absent", "v1"))

        assertIs<DataResult.Success<*>>(result)
        assertEquals(listOf("v2", "v1"), (result as DataResult.Success).data.map { it.id })
    }

    @Test
    fun `transforme un JSON invalide en erreur`() = runTest {
        val repository = VerseBankRepositoryImpl(FakeDataSource("pas du json"))

        assertIs<DataResult.Error>(repository.getThemes())
    }
}
