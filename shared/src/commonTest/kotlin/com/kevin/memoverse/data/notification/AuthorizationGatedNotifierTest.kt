package com.kevin.memoverse.data.notification

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthorizationGatedNotifierTest {

    private val now = 1_000_000L
    private val center = FakeNotificationCenter()
    private val notifier = AuthorizationGatedNotifier(center, nowEpochMillis = { now })

    private fun reminder(id: Int, inSeconds: Long = 60) =
        LocalNotification(id = id, atEpochMillis = now + inSeconds * 1000, title = "Titre $id", body = "Corps $id")

    @Test
    fun `sans autorisation les rappels sont retenus sans être programmés`() = runTest {
        notifier.replaceAll(listOf(reminder(1)))

        assertEquals(listOf<Call>(Removed(AuthorizationGatedNotifier.IDS)), center.calls)
    }

    @Test
    fun `avec autorisation les anciens rappels sont retirés avant de poser les nouveaux`() = runTest {
        center.authorized = true

        notifier.replaceAll(listOf(reminder(1, inSeconds = 60), reminder(2, inSeconds = 3600)))

        assertEquals(
            listOf(
                Removed(AuthorizationGatedNotifier.IDS),
                Added("memoverse-review-1", "Titre 1", "Corps 1", 60.0),
                Added("memoverse-review-2", "Titre 2", "Corps 2", 3600.0),
            ),
            center.calls,
        )
    }

    @Test
    fun `un rappel déjà passé est programmé avec le délai minimal d'une seconde`() = runTest {
        center.authorized = true

        notifier.replaceAll(listOf(reminder(1, inSeconds = -30)))

        assertEquals(AuthorizationGatedNotifier.MIN_DELAY_SECONDS, center.calls.filterIsInstance<Added>().single().delaySeconds)
    }

    @Test
    fun `une liste vide retire seulement les rappels en attente`() = runTest {
        center.authorized = true

        notifier.replaceAll(emptyList())

        assertEquals(listOf<Call>(Removed(AuthorizationGatedNotifier.IDS)), center.calls)
    }

    @Test
    fun `accepter l'autorisation programme les derniers rappels retenus`() = runTest {
        notifier.replaceAll(listOf(reminder(1)))
        notifier.replaceAll(listOf(reminder(2), reminder(3)))
        center.calls.clear()
        center.grants = true

        notifier.requestAuthorization()

        assertEquals(1, center.requestCount)
        assertEquals(
            listOf(
                Removed(AuthorizationGatedNotifier.IDS),
                Added("memoverse-review-2", "Titre 2", "Corps 2", 60.0),
                Added("memoverse-review-3", "Titre 3", "Corps 3", 60.0),
            ),
            center.calls,
        )
    }

    @Test
    fun `refuser l'autorisation ne touche à rien`() = runTest {
        notifier.replaceAll(listOf(reminder(1)))
        center.calls.clear()

        notifier.requestAuthorization()

        assertEquals(1, center.requestCount)
        assertTrue(center.calls.isEmpty())
    }

    @Test
    fun `accepter sans rappel retenu retire seulement les rappels en attente`() = runTest {
        center.grants = true

        notifier.requestAuthorization()

        assertEquals(listOf<Call>(Removed(AuthorizationGatedNotifier.IDS)), center.calls)
    }

    private sealed interface Call
    private data class Removed(val identifiers: List<String>) : Call
    private data class Added(val identifier: String, val title: String, val body: String, val delaySeconds: Double) : Call

    /** Centre de notifications en mémoire : enregistre les retraits et ajouts dans l'ordre. */
    private class FakeNotificationCenter : NotificationCenter {
        var authorized = false
        var grants = false
        var requestCount = 0
        val calls = mutableListOf<Call>()

        override suspend fun isAuthorized() = authorized

        override suspend fun requestAuthorization(): Boolean {
            requestCount++
            return grants
        }

        override fun removePending(identifiers: List<String>) {
            calls += Removed(identifiers)
        }

        override fun add(identifier: String, title: String, body: String, delaySeconds: Double) {
            calls += Added(identifier, title, body, delaySeconds)
        }
    }
}
