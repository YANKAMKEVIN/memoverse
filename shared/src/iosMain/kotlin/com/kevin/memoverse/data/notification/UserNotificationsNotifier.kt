package com.kevin.memoverse.data.notification

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

/** Rappels locaux iOS ; l'autorisation est demandée au premier rappel programmé. */
class UserNotificationsNotifier : LocalNotifier {

    private val center get() = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun replaceAll(notifications: List<LocalNotification>) {
        center.removePendingNotificationRequestsWithIdentifiers(IDS)
        if (notifications.isEmpty() || !requestAuthorization()) return
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

    private suspend fun requestAuthorization(): Boolean = suspendCancellableCoroutine { continuation ->
        center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge) { granted, _ ->
            continuation.resume(granted)
        }
    }

    private companion object {
        const val MIN_DELAY_SECONDS = 1.0
        const val PREFIX = "memoverse-review-"
        val IDS = (1..4).map { "$PREFIX$it" }

        fun id(index: Int) = "$PREFIX$index"
    }
}
