package com.kevin.memoverse.data.notification

import com.kevin.memoverse.domain.model.HiddenPosition
import com.kevin.memoverse.domain.notification.PlannedNotification
import com.kevin.memoverse.domain.repository.NotificationScheduler
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.app_wordmark
import com.kevin.memoverse.resources.notification_part_end
import com.kevin.memoverse.resources.notification_part_middle
import com.kevin.memoverse.resources.notification_part_start
import com.kevin.memoverse.resources.notification_several_thirsty
import com.kevin.memoverse.resources.notification_single_thirsty
import org.jetbrains.compose.resources.getString

/** Rédige les rappels (« Hébreux 11:1 a soif… ») puis les confie à la plateforme. */
class ReviewNotificationScheduler(
    private val notifier: LocalNotifier,
) : NotificationScheduler {

    override suspend fun replaceAll(notifications: List<PlannedNotification>) {
        val title = getString(Res.string.app_wordmark)
        notifier.replaceAll(
            notifications.mapIndexed { index, planned ->
                LocalNotification(id = index + 1, atEpochMillis = planned.atEpochMillis, title = title, body = body(planned))
            },
        )
    }

    private suspend fun body(planned: PlannedNotification): String = when (planned.references.size) {
        1 -> getString(Res.string.notification_single_thirsty, planned.references.single(), getString(planned.part.label()))
        else -> getString(Res.string.notification_several_thirsty, planned.references.size)
    }

    private fun HiddenPosition.label() = when (this) {
        HiddenPosition.START -> Res.string.notification_part_start
        HiddenPosition.MIDDLE -> Res.string.notification_part_middle
        HiddenPosition.END -> Res.string.notification_part_end
    }
}
