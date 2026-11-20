package com.kevin.memoverse.presentation.garden

import com.kevin.memoverse.domain.model.Garden
import com.kevin.memoverse.domain.model.GardenPlant
import com.kevin.memoverse.domain.model.MasteryStage
import kotlin.test.Test
import kotlin.test.assertEquals

class GardenUiStateMapperTest {

    private val hour = 3_600_000L

    @Test
    fun `sous une heure - le prochain arrosage s'affiche en minutes`() {
        // Régression QA : 50 s s'affichaient « dans 1 h ».
        assertEquals(NextWateringUi.InMinutes(1), 50_000L.toNextWatering())
        assertEquals(NextWateringUi.InMinutes(40), (40 * 60_000L).toNextWatering())
    }

    @Test
    fun `arrondit le prochain arrosage à l'heure supérieure`() {
        assertEquals(NextWateringUi.InHours(1), hour.toNextWatering())
        assertEquals(NextWateringUi.InHours(6), (5 * hour + 1).toNextWatering())
    }

    @Test
    fun `passe en jours au-delà de 24 h`() {
        assertEquals(NextWateringUi.InDays(1), (24 * hour).toNextWatering())
        assertEquals(NextWateringUi.InDays(3), (60 * hour).toNextWatering())
    }

    @Test
    fun `chaque verset donne son échéance sauf s'il a déjà soif`() {
        val state = Garden(
            plants = listOf(
                GardenPlant("a", "A", MasteryStage.SEED, isThirsty = true),
                GardenPlant("b", "B", MasteryStage.ROOT, millisUntilThirsty = 30 * hour),
            ),
            millisUntilNextWatering = null,
        ).toGardenUiState()

        assertEquals(listOf(null, NextWateringUi.InDays(2)), state.plants.map { it.nextWatering })
    }
}
