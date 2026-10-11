package com.kevin.memoverse.domain.exercise

import com.kevin.memoverse.domain.model.MasteryStage
import com.kevin.memoverse.fake.learningVerse
import kotlin.test.Test
import kotlin.test.assertEquals

class ExercisePlannerTest {

    @Test
    fun `une graine commence par les niveaux 1 et 2`() {
        assertEquals(listOf(1, 2), ExercisePlanner.levelsFor(learningVerse("v1")))
    }

    @Test
    fun `les niveaux avancent avec les réussites du stade`() {
        assertEquals(listOf(3, 1), ExercisePlanner.levelsFor(learningVerse("v1").copy(successCount = 2)))
    }

    @Test
    fun `une racine écrit elle-même, un arbre récite, un fruit récite aussi à l'oral`() {
        assertEquals(listOf(4, 5), ExercisePlanner.levelsFor(learningVerse("v1", MasteryStage.ROOT)))
        assertEquals(listOf(6, 7), ExercisePlanner.levelsFor(learningVerse("v1", MasteryStage.TREE)))
        assertEquals(listOf(8, 6), ExercisePlanner.levelsFor(learningVerse("v1", MasteryStage.TREE).copy(successCount = 2)))
        assertEquals(listOf(7, 8), ExercisePlanner.levelsFor(learningVerse("v1", MasteryStage.FRUIT)))
    }
}
