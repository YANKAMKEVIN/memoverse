package com.kevin.memoverse.data.mapper

import com.kevin.memoverse.data.local.db.LearningVerseEntity
import com.kevin.memoverse.domain.model.MasteryStage
import kotlin.test.Test
import kotlin.test.assertEquals

class LearningVerseMapperTest {

    private fun entity(stage: String) = LearningVerseEntity(
        verseId = "jean-3-16",
        themeId = "salut",
        stage = stage,
        addedAtEpochMillis = 42L,
        successCount = 2,
        successDays = 1,
        lastSuccessEpochDay = 20_000L,
        consecutiveFailures = 1,
        reviewStep = 3,
        dueAtEpochMillis = 123L,
    )

    @Test
    fun `relit le stade enregistré`() {
        assertEquals(MasteryStage.TREE, entity("TREE").toDomain().stage)
    }

    @Test
    fun `relit la progression dans le stade`() {
        val verse = entity("ROOT").toDomain()

        assertEquals(2, verse.successCount)
        assertEquals(1, verse.successDays)
        assertEquals(20_000L, verse.lastSuccessEpochDay)
        assertEquals(1, verse.consecutiveFailures)
        assertEquals(3, verse.reviewStep)
        assertEquals(123L, verse.dueAtEpochMillis)
    }

    @Test
    fun `retombe sur graine si le stade est inconnu`() {
        assertEquals(MasteryStage.SEED, entity("???").toDomain().stage)
    }
}
