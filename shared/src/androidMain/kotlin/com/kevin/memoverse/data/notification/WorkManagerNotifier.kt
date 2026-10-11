package com.kevin.memoverse.data.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

/** Programme chaque rappel comme un travail WorkManager (conservé après un redémarrage). */
class WorkManagerNotifier(
    private val context: Context,
    private val nowEpochMillis: () -> Long = System::currentTimeMillis,
) : LocalNotifier {

    override suspend fun replaceAll(notifications: List<LocalNotification>) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(TAG)
        notifications.forEach { notification ->
            val request = OneTimeWorkRequestBuilder<ReviewNotificationWorker>()
                .setInitialDelay((notification.atEpochMillis - nowEpochMillis()).coerceAtLeast(0), TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(
                        ReviewNotificationWorker.KEY_ID to notification.id,
                        ReviewNotificationWorker.KEY_TITLE to notification.title,
                        ReviewNotificationWorker.KEY_BODY to notification.body,
                    ),
                )
                .addTag(TAG)
                .build()
            workManager.enqueueUniqueWork("$TAG-${notification.id}", ExistingWorkPolicy.REPLACE, request)
        }
    }

    private companion object {
        const val TAG = "review-notification"
    }
}
