package com.kevin.memoverse.presentation.progression

import com.kevin.memoverse.domain.model.MasteryStage
import com.kevin.memoverse.fake.learningVerse
import kotlin.test.Test
import kotlin.test.assertEquals

class ProgressionUiStateMapperTest {

    @Test
    fun `compte les versets par stade`() {
        val state = listOf(
            learningVerse("a", MasteryStage.SEED),
            learningVerse("b", MasteryStage.SEED),
            learningVerse("c", MasteryStage.ROOT),
            learningVerse("d", MasteryStage.FRUIT),
        ).toProgressionUiState()

        assertEquals(ProgressionUiState(isLoading = false, seedCount = 2, rootCount = 1, treeCount = 0, fruitCount = 1), state)
        assertEquals(4, state.totalCount)
    }

    @Test
    fun `un jardin vide donne des compteurs à zéro`() {
        assertEquals(0, emptyList<com.kevin.memoverse.domain.model.LearningVerse>().toProgressionUiState().totalCount)
    }
}
