package com.kevin.memoverse.data.notification

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

/**
 * Rappels locaux iOS. L'autorisation n'est demandée qu'après la première plantation
 * ([requestAuthorization]) : d'ici là, les rappels sont seulement retenus, puis programmés dès qu'elle est accordée.
 */
class UserNotificationsNotifier : LocalNotifier {

    private val center get() = UNUserNotificationCenter.currentNotificationCenter()
    private val mutex = Mutex()
    private var latest: List<LocalNotification> = emptyList()

    override suspend fun replaceAll(notifications: List<LocalNotification>) = mutex.withLock {
        latest = notifications
        schedule(notifications, isAuthorized())
    }

    /** Pose la question à l'utilisateur ; s'il accepte, les rappels déjà prévus sont programmés. */
    suspend fun requestAuthorization() {
        val granted = suspendCancellableCoroutine { continuation ->
            center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge) { granted, _ ->
                continuation.resume(granted)
            }
        }
        if (granted) mutex.withLock { schedule(latest, isAuthorized = true) }
    }

    private fun schedule(notifications: List<LocalNotification>, isAuthorized: Boolean) {
        center.removePendingNotificationRequestsWithIdentifiers(IDS)
        if (notifications.isEmpty() || !isAuthorized) return
        val nowMillis = (NSDate().timeIntervalSince1970 * 1000).toLong()
        notifications.forEach { notification ->
            val content = UNMutableNotificationContent().apply {
                setTitle(notification.title)
                setBody(notification.body)
                setSound(UNNotificationSound.defaultSound)
            }
            val delaySeconds = ((notification.atEpochMillis - nowMillis) / 1000.0).coerceAtLeast(MIN_DELAY_SECONDS)
            val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(delaySeconds, repeats = false)
            center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier(id(notification.id), content, trigger), null)
        }
    }

    private suspend fun isAuthorized(): Boolean = suspendCancellableCoroutine { continuation ->
        center.getNotificationSettingsWithCompletionHandler { settings ->
            continuation.resume(settings?.authorizationStatus in AUTHORIZED)
        }
    }

    private companion object {
        const val MIN_DELAY_SECONDS = 1.0
        const val PREFIX = "memoverse-review-"
        val IDS = (1..4).map { "$PREFIX$it" }
        val AUTHORIZED = setOf(UNAuthorizationStatusAuthorized, UNAuthorizationStatusProvisional, UNAuthorizationStatusEphemeral)

        fun id(index: Int) = "$PREFIX$index"
    }
}
