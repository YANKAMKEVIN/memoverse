package com.kevin.memoverse.domain.repository

import com.kevin.memoverse.domain.notification.PlannedNotification

/** Programme les rappels sur l'appareil ; chaque appel remplace les rappels précédents. */
interface NotificationScheduler {
    suspend fun replaceAll(notifications: List<PlannedNotification>)
}
