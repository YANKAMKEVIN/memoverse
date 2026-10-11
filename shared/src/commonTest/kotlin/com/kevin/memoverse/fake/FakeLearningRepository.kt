package com.kevin.memoverse.fake

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.LearningVerse
import com.kevin.memoverse.domain.model.MasteryStage
import com.kevin.memoverse.domain.repository.LearningRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeLearningRepository(
    initial: List<LearningVerse> = emptyList(),
    var saveError: Throwable? = null,
) : LearningRepository {

    val verses = MutableStateFlow(initial)

    override fun observeLearningVerses(): Flow<List<LearningVerse>> = verses

    override suspend fun addVerses(verses: List<LearningVerse>): DataResult<Unit> {
        saveError?.let { return DataResult.Error(it) }
        this.verses.update { current -> current + verses.filter { new -> current.none { it.verseId == new.verseId } } }
        return DataResult.Success(Unit)
    }

    override suspend fun clear(): DataResult<Unit> {
        saveError?.let { return DataResult.Error(it) }
        verses.value = emptyList()
        return DataResult.Success(Unit)
    }

    override suspend fun updateVerse(verse: LearningVerse): DataResult<Unit> {
        saveError?.let { return DataResult.Error(it) }
        verses.update { current -> current.map { if (it.verseId == verse.verseId) verse else it } }
        return DataResult.Success(Unit)
    }
}

fun learningVerse(verseId: String, stage: MasteryStage = MasteryStage.SEED, themeId: String = "foi") =
    LearningVerse(verseId = verseId, themeId = themeId, stage = stage, addedAtEpochMillis = 0L)
