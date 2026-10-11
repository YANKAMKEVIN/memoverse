package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.VerseSelection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ToggleVerseSelectionUseCaseTest {

    private val toggle = ToggleVerseSelectionUseCase()

    @Test
    fun `ajoute un verset non sélectionné`() {
        val result = toggle(VerseSelection(), "v1")

        assertEquals(setOf("v1"), result.selectedIds)
    }

    @Test
    fun `retire un verset déjà sélectionné et efface l'alerte de limite`() {
        val result = toggle(VerseSelection(setOf("v1", "v2"), isLimitReached = true), "v1")

        assertEquals(setOf("v2"), result.selectedIds)
        assertFalse(result.isLimitReached)
    }

    @Test
    fun `refuse un cinquième verset et signale la limite`() {
        val full = VerseSelection(setOf("v1", "v2", "v3", "v4"))

        val result = toggle(full, "v5")

        assertEquals(full.selectedIds, result.selectedIds)
        assertTrue(result.isLimitReached)
    }

    @Test
    fun `permet de commencer seulement avec 3 ou 4 versets`() {
        assertFalse(VerseSelection(setOf("v1", "v2")).canStart)
        assertTrue(VerseSelection(setOf("v1", "v2", "v3")).canStart)
        assertTrue(VerseSelection(setOf("v1", "v2", "v3", "v4")).canStart)
    }
}
