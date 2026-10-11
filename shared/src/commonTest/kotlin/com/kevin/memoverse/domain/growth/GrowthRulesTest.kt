package com.kevin.memoverse.domain.growth

import com.kevin.memoverse.domain.model.MasteryStage
import com.kevin.memoverse.fake.learningVerse
import kotlin.test.Test
import kotlin.test.assertEquals

class GrowthRulesTest {

    private val day = 20_000L

    @Test
    fun `une graine devient racine après 3 réussites`() {
        var verse = learningVerse("v1")
        repeat(2) { verse = GrowthRules.apply(verse, isCorrect = true, todayEpochDay = day) }
        assertEquals(MasteryStage.SEED, verse.stage)
        assertEquals(2, verse.successCount)

        verse = GrowthRules.apply(verse, isCorrect = true, todayEpochDay = day)

        assertEquals(MasteryStage.ROOT, verse.stage)
        assertEquals(0, verse.successCount)
    }

    @Test
    fun `une racine a besoin de 4 réussites sur 2 jours différents`() {
        var verse = learningVerse("v1", MasteryStage.ROOT)
        repeat(5) { verse = GrowthRules.apply(verse, isCorrect = true, todayEpochDay = day) }
        assertEquals(MasteryStage.ROOT, verse.stage)

        verse = GrowthRules.apply(verse, isCorrect = true, todayEpochDay = day + 1)

        assertEquals(MasteryStage.TREE, verse.stage)
    }

    @Test
    fun `un arbre ne compte qu'une réussite tous les 3 jours`() {
        var verse = learningVerse("v1", MasteryStage.TREE)
        verse = GrowthRules.apply(verse, isCorrect = true, todayEpochDay = day)
        verse = GrowthRules.apply(verse, isCorrect = true, todayEpochDay = day + 1)
        assertEquals(1, verse.successCount)

        verse = GrowthRules.apply(verse, isCorrect = true, todayEpochDay = day + 3)
        verse = GrowthRules.apply(verse, isCorrect = true, todayEpochDay = day + 6)

        assertEquals(MasteryStage.FRUIT, verse.stage)
    }

    @Test
    fun `deux échecs d'affilée font redescendre d'un stade`() {
        var verse = learningVerse("v1", MasteryStage.ROOT)
        verse = GrowthRules.apply(verse, isCorrect = false, todayEpochDay = day)
        assertEquals(MasteryStage.ROOT, verse.stage)

        verse = GrowthRules.apply(verse, isCorrect = false, todayEpochDay = day)

        assertEquals(MasteryStage.SEED, verse.stage)
        assertEquals(0, verse.consecutiveFailures)
    }

    @Test
    fun `une réussite remet le compteur d'échecs à zéro`() {
        var verse = learningVerse("v1", MasteryStage.ROOT)
        verse = GrowthRules.apply(verse, isCorrect = false, todayEpochDay = day)
        verse = GrowthRules.apply(verse, isCorrect = true, todayEpochDay = day)
        verse = GrowthRules.apply(verse, isCorrect = false, todayEpochDay = day)

        assertEquals(MasteryStage.ROOT, verse.stage)
    }

    @Test
    fun `une graine ne descend jamais plus bas`() {
        var verse = learningVerse("v1")
        repeat(4) { verse = GrowthRules.apply(verse, isCorrect = false, todayEpochDay = day) }

        assertEquals(MasteryStage.SEED, verse.stage)
    }
}
