package com.kevin.memoverse.data.notification

/** Notification prête à afficher, déjà rédigée. */
data class LocalNotification(
    val id: Int,
    val atEpochMillis: Long,
    val title: String,
    val body: String,
)

/** Accès au système de notifications locales de la plateforme (WorkManager, UNUserNotificationCenter). */
interface LocalNotifier {
    suspend fun replaceAll(notifications: List<LocalNotification>)
}
