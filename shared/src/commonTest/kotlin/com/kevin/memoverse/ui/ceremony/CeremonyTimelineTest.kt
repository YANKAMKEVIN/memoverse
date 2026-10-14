package com.kevin.memoverse.ui.ceremony

import com.kevin.memoverse.presentation.ceremony.CeremonyUi
import com.kevin.memoverse.presentation.common.StageUi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CeremonyTimelineTest {

    private val stages = StageUi.entries

    @Test
    fun `plantation et arrosage durent entre 3 et 5 secondes`() {
        val ceremonies = (1..MAX_SEEDS).map { n -> CeremonyUi.Planting(List(n) { "Référence $it" }) } +
            stages.flatMap { from -> stages.filter { it >= from }.map { to -> CeremonyUi.Watering("Référence", from, to) } }

        ceremonies.forEach { ceremony ->
            val total = CeremonyTimeline.of(ceremony).totalMillis
            assertTrue(total in 3_000..5_000, "$ceremony dure $total ms")
        }
    }

    @Test
    fun `la variante qui fane reste brève`() {
        stages.forEach { from ->
            stages.filter { it < from }.forEach { to ->
                assertTrue(CeremonyTimeline.of(CeremonyUi.Wilting("Référence", from, to)).totalMillis <= 3_000)
            }
        }
    }

    @Test
    fun `l'arrosage repart de la graine et finit au stade atteint`() {
        val timeline = WateringTimeline(StageUi.ROOT, StageUi.TREE)

        assertEquals(0f, timeline.growth(0f))
        assertEquals(StageUi.ROOT.level, timeline.growth(timeline.evocation.end))
        assertEquals(StageUi.TREE.level, timeline.growth(timeline.totalMillis.toFloat()))
    }

    @Test
    fun `sans changement de stade la croissance s'arrête au stade actuel`() {
        val timeline = WateringTimeline(StageUi.FRUIT, StageUi.FRUIT)

        assertEquals(StageUi.FRUIT.level, timeline.growth(timeline.totalMillis.toFloat()))
        assertEquals(timeline.evocation.end, timeline.emphasis.end)
    }

    @Test
    fun `le passage au nouveau stade prend son temps après l'évocation`() {
        val timeline = WateringTimeline(StageUi.SEED, StageUi.ROOT)

        assertTrue(timeline.emphasis.end - timeline.emphasis.start >= 1_000f)
        assertTrue(timeline.pour.end <= timeline.emphasis.end)
    }

    @Test
    fun `les graines tombent l'une après l'autre et sont enfouies avant la fin`() {
        val timeline = PlantingTimeline(seedCount = 4)

        (1 until 4).forEach { assertTrue(timeline.fall(it).start > timeline.fall(it - 1).start) }
        assertTrue(timeline.sink(3).end <= timeline.glow.end)
        assertTrue(timeline.glow.end < timeline.totalMillis)
    }
}
