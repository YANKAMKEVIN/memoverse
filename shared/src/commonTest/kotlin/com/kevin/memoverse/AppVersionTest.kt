package com.kevin.memoverse

import kotlin.test.Test
import kotlin.test.assertTrue

class AppVersionTest {

    @Test
    fun `la version affichée vient de gradle et suit majeur point mineur point correctif`() {
        assertTrue(Regex("""\d+\.\d+\.\d+""").matches(AppVersion.NAME), "Version inattendue : ${AppVersion.NAME}")
    }
}
