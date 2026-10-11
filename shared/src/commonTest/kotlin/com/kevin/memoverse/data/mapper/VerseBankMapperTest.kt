package com.kevin.memoverse.data.mapper

import com.kevin.memoverse.data.local.VerseDto
import com.kevin.memoverse.domain.model.Difficulty
import kotlin.test.Test
import kotlin.test.assertEquals

class VerseBankMapperTest {

    private fun dto(difficulty: String) = VerseDto(
        id = "jean-3-16",
        reference = "Jean 3:16",
        text = "Car Dieu a tant aimé le monde…",
        context = "Jésus parle avec Nicodème.",
        wordCount = 30,
        difficulty = difficulty,
        themes = listOf("amour-de-dieu", "salut"),
    )

    @Test
    fun `convertit les libellés de difficulté de la banque`() {
        assertEquals(Difficulty.EASY, dto("facile").toDomain().difficulty)
        assertEquals(Difficulty.MEDIUM, dto("moyen").toDomain().difficulty)
        assertEquals(Difficulty.HARD, dto("difficile").toDomain().difficulty)
    }

    @Test
    fun `considère une difficulté inconnue comme moyenne`() {
        assertEquals(Difficulty.MEDIUM, dto("???").toDomain().difficulty)
    }

    @Test
    fun `reprend les thèmes du verset`() {
        assertEquals(listOf("amour-de-dieu", "salut"), dto("facile").toDomain().themeIds)
    }
}
