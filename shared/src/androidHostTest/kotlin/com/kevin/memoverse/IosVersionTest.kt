package com.kevin.memoverse

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Xcode ne lit pas gradle.properties : la version iOS (MARKETING_VERSION dans Config.xcconfig, reprise par
 * CFBundleShortVersionString d'Info.plist) est recopiée à la main. Ce test échoue dès que les deux divergent.
 */
class IosVersionTest {

    private val root: File = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "iosApp").isDirectory && File(it, "gradle.properties").isFile }

    @Test
    fun `la version iOS est celle de gradle properties`() {
        val gradleVersion = File(root, "gradle.properties").readLines()
            .firstNotNullOfOrNull { Regex("""^memoverse\.versionName\s*=\s*(\S+)\s*$""").find(it)?.groupValues?.get(1) }
        val iosVersion = File(root, "iosApp/Configuration/Config.xcconfig").readLines()
            .firstNotNullOfOrNull { Regex("""^MARKETING_VERSION\s*=\s*(\S+)\s*$""").find(it)?.groupValues?.get(1) }
        assertNotNull(gradleVersion, "memoverse.versionName absent de gradle.properties")
        assertEquals(gradleVersion, iosVersion, "MARKETING_VERSION de Config.xcconfig doit valoir memoverse.versionName")
        assertEquals(AppVersion.NAME, iosVersion, "L'écran Profil et iOS doivent annoncer la même version")
    }

    @Test
    fun `Info plist lit la version dans MARKETING_VERSION`() {
        val plist = File(root, "iosApp/iosApp/Info.plist").readText()
        val value = Regex("""<key>CFBundleShortVersionString</key>\s*<string>([^<]*)</string>""").find(plist)?.groupValues?.get(1)
        assertEquals("\$(MARKETING_VERSION)", value)
    }
}
