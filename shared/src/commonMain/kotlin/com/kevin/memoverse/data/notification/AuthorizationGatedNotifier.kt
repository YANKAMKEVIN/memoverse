package com.kevin.memoverse.data.notification

import com.kevin.memoverse.data.system.SystemTimeProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Centre de notifications d'une plateforme qui exige une autorisation explicite (UNUserNotificationCenter). */
interface NotificationCenter {
    suspend fun isAuthorized(): Boolean

    /** Pose la question à l'utilisateur ; vrai s'il accepte. */
    suspend fun requestAuthorization(): Boolean

    fun removePending(identifiers: List<String>)

    fun add(identifier: String, title: String, body: String, delaySeconds: Double)
}

/**
 * Rappels locaux soumis à autorisation. L'autorisation n'est demandée qu'après la première plantation
 * ([requestAuthorization]) : d'ici là, les rappels sont seulement retenus, puis programmés dès qu'elle est accordée.
 */
class AuthorizationGatedNotifier(
    private val center: NotificationCenter,
    private val nowEpochMillis: () -> Long = SystemTimeProvider()::nowEpochMillis,
) : LocalNotifier {

    private val mutex = Mutex()
    private var latest: List<LocalNotification> = emptyList()

    override suspend fun replaceAll(notifications: List<LocalNotification>) = mutex.withLock {
        latest = notifications
        schedule(notifications, center.isAuthorized())
    }

    /** Pose la question à l'utilisateur ; s'il accepte, les rappels déjà prévus sont programmés. */
    suspend fun requestAuthorization() {
        if (center.requestAuthorization()) mutex.withLock { schedule(latest, isAuthorized = true) }
    }

    private fun schedule(notifications: List<LocalNotification>, isAuthorized: Boolean) {
        center.removePending(IDS)
        if (notifications.isEmpty() || !isAuthorized) return
        val nowMillis = nowEpochMillis()
        notifications.forEach { notification ->
            val delaySeconds = ((notification.atEpochMillis - nowMillis) / 1000.0).coerceAtLeast(MIN_DELAY_SECONDS)
            center.add(id(notification.id), notification.title, notification.body, delaySeconds)
        }
    }

    internal companion object {
        const val MIN_DELAY_SECONDS = 1.0
        const val PREFIX = "memoverse-review-"
        val IDS = (1..4).map { "$PREFIX$it" }

        fun id(index: Int) = "$PREFIX$index"
    }
}
