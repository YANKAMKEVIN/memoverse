package com.kevin.memoverse.data.repository

import com.kevin.memoverse.data.local.VerseBankDataSource
import com.kevin.memoverse.data.local.VerseBankDto
import com.kevin.memoverse.data.mapper.toDomain
import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.Theme
import com.kevin.memoverse.domain.model.Verse
import com.kevin.memoverse.domain.repository.VerseBankRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

/** Charge la banque une seule fois (environ 200 Ko) puis la garde en mémoire. */
class VerseBankRepositoryImpl(
    private val dataSource: VerseBankDataSource,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : VerseBankRepository {

    private val mutex = Mutex()
    private var cache: VerseBank? = null

    override suspend fun getThemes(): DataResult<List<Theme>> =
        withBank { it.themes }

    override suspend fun getVerses(verseIds: List<String>): DataResult<List<Verse>> =
        withBank { bank -> verseIds.mapNotNull { bank.versesById[it] } }

    override suspend fun getAllVerses(): DataResult<List<Verse>> =
        withBank { it.versesById.values.toList() }

    private suspend fun <T> withBank(block: (VerseBank) -> T): DataResult<T> = try {
        DataResult.Success(block(loadBank()))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DataResult.Error(e)
    }

    private suspend fun loadBank(): VerseBank = mutex.withLock {
        cache ?: json.decodeFromString<VerseBankDto>(dataSource.readVerseBankJson())
            .toVerseBank()
            .also { cache = it }
    }

    private fun VerseBankDto.toVerseBank() = VerseBank(
        themes = themes.map { it.toDomain() },
        versesById = verses.associate { it.id to it.toDomain() },
    )

    private class VerseBank(
        val themes: List<Theme>,
        val versesById: Map<String, Verse>,
    )
}
