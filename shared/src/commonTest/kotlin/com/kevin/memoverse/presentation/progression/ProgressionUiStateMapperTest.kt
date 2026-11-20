package com.kevin.memoverse.presentation.progression

import com.kevin.memoverse.domain.model.Garden
import com.kevin.memoverse.domain.model.GardenPlant
import com.kevin.memoverse.domain.model.MasteryStage
import com.kevin.memoverse.presentation.common.StageUi
import com.kevin.memoverse.presentation.garden.NextWateringUi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProgressionUiStateMapperTest {

    private val hour = 3_600_000L

    private fun plant(id: String, stage: MasteryStage, thirsty: Boolean = false, dueIn: Long = 0) =
        GardenPlant(id, "Réf $id", stage, isThirsty = thirsty, text = "Texte $id", millisUntilThirsty = dueIn)

    @Test
    fun `compte les versets par stade`() {
        val state = Garden(
            plants = listOf(
                plant("a", MasteryStage.SEED, thirsty = true),
                plant("b", MasteryStage.SEED, thirsty = true),
                plant("c", MasteryStage.ROOT, thirsty = true),
                plant("d", MasteryStage.FRUIT, thirsty = true),
            ),
            millisUntilNextWatering = null,
        ).toProgressionUiState()

        assertEquals(listOf(2, 1, 0, 1), listOf(state.seedCount, state.rootCount, state.treeCount, state.fruitCount))
        assertEquals(4, state.totalCount)
    }

    @Test
    fun `regroupe les versets par stade de Graine à Fruit sans stade vide`() {
        val state = Garden(
            plants = listOf(plant("fruit", MasteryStage.FRUIT, dueIn = hour), plant("graine", MasteryStage.SEED, dueIn = hour)),
            millisUntilNextWatering = hour,
        ).toProgressionUiState()

        assertEquals(listOf(StageUi.SEED, StageUi.FRUIT), state.groups.map { it.stage })
    }

    @Test
    fun `dans un stade ceux qui ont soif passent en premier puis l'échéance la plus proche`() {
        val state = Garden(
            plants = listOf(
                plant("tard", MasteryStage.ROOT, dueIn = 50 * hour),
                plant("soif", MasteryStage.ROOT, thirsty = true),
                plant("bientot", MasteryStage.ROOT, dueIn = 2 * hour),
            ),
            millisUntilNextWatering = null,
        ).toProgressionUiState()

        val plants = state.groups.single().plants
        assertEquals(listOf("soif", "bientot", "tard"), plants.map { it.verseId })
        assertNull(plants[0].nextWatering)
        assertEquals(NextWateringUi.InHours(2), plants[1].nextWatering)
        assertEquals(NextWateringUi.InDays(3), plants[2].nextWatering)
        assertEquals("Texte tard", plants[2].text)
    }

    @Test
    fun `un jardin vide donne des compteurs à zéro`() {
        val state = Garden(plants = emptyList(), millisUntilNextWatering = null).toProgressionUiState()

        assertEquals(0, state.totalCount)
        assertEquals(emptyList(), state.groups)
    }
}
