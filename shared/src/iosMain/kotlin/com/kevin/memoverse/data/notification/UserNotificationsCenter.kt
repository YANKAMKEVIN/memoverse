package com.kevin.memoverse.data.notification

import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

/** Accès à UNUserNotificationCenter ; quand retenir ou programmer les rappels est décidé par [AuthorizationGatedNotifier]. */
class UserNotificationsCenter : NotificationCenter {

    private val center get() = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun isAuthorized(): Boolean = suspendCancellableCoroutine { continuation ->
        center.getNotificationSettingsWithCompletionHandler { settings ->
            continuation.resume(settings?.authorizationStatus in AUTHORIZED)
        }
    }

    /** null tant que la question n'a pas été posée ; sinon, rappels autorisés ou non (ligne « Rappels » du Profil). */
    suspend fun authorization(): Boolean? = suspendCancellableCoroutine { continuation ->
        center.getNotificationSettingsWithCompletionHandler { settings ->
            continuation.resume(
                when (settings?.authorizationStatus) {
                    UNAuthorizationStatusNotDetermined -> null
                    in AUTHORIZED -> true
                    else -> false
                },
            )
        }
    }

    override suspend fun requestAuthorization(): Boolean = suspendCancellableCoroutine { continuation ->
        center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge) { granted, _ ->
            continuation.resume(granted)
        }
    }

    override fun removePending(identifiers: List<String>) {
        center.removePendingNotificationRequestsWithIdentifiers(identifiers)
    }

    override fun add(identifier: String, title: String, body: String, delaySeconds: Double) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound)
        }
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(delaySeconds, repeats = false)
        center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier(identifier, content, trigger), null)
    }

    private companion object {
        val AUTHORIZED = setOf(UNAuthorizationStatusAuthorized, UNAuthorizationStatusProvisional, UNAuthorizationStatusEphemeral)
    }
}
