package com.kevin.memoverse.fake

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.Difficulty
import com.kevin.memoverse.domain.model.Theme
import com.kevin.memoverse.domain.model.Verse
import com.kevin.memoverse.domain.repository.VerseBankRepository

class FakeVerseBankRepository(
    var themes: List<Theme> = emptyList(),
    var verses: List<Verse> = emptyList(),
    var error: Throwable? = null,
) : VerseBankRepository {

    override suspend fun getThemes(): DataResult<List<Theme>> =
        error?.let { DataResult.Error(it) } ?: DataResult.Success(themes)

    override suspend fun getVerses(verseIds: List<String>): DataResult<List<Verse>> =
        error?.let { DataResult.Error(it) } ?: DataResult.Success(verseIds.mapNotNull { id -> verses.find { it.id == id } })

    override suspend fun getAllVerses(): DataResult<List<Verse>> =
        error?.let { DataResult.Error(it) } ?: DataResult.Success(verses)
}

fun theme(id: String, family: String = "Relation avec Dieu", verseIds: List<String> = emptyList()) =
    Theme(id = id, name = id.replaceFirstChar { it.uppercase() }, family = family, verseIds = verseIds)

fun verse(id: String, difficulty: Difficulty = Difficulty.EASY, text: String = "Texte $id") =
    Verse(
        id = id,
        reference = "Référence $id",
        text = text,
        context = "Contexte $id",
        wordCount = 10,
        difficulty = difficulty,
        themeIds = emptyList(),
    )
