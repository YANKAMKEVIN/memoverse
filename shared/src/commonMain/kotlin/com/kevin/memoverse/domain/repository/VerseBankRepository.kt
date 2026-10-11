package com.kevin.memoverse.domain.repository

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.Theme
import com.kevin.memoverse.domain.model.Verse

/** Banque de versets embarquée dans l'app (lecture seule). */
interface VerseBankRepository {
    suspend fun getThemes(): DataResult<List<Theme>>
    suspend fun getVerses(verseIds: List<String>): DataResult<List<Verse>>
    suspend fun getAllVerses(): DataResult<List<Verse>>
}
