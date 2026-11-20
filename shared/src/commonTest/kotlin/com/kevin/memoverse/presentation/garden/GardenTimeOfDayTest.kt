package com.kevin.memoverse.presentation.garden

import kotlin.test.Test
import kotlin.test.assertEquals

class GardenTimeOfDayTest {

    @Test
    fun `la nuit va de 20 h à 6 h`() {
        val nightHours = (0..23).filter { isNightHour(it) }

        assertEquals(listOf(0, 1, 2, 3, 4, 5, 20, 21, 22, 23), nightHours)
    }
}
