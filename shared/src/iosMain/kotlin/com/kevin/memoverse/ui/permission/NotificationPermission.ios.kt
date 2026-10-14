package com.kevin.memoverse.ui.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.kevin.memoverse.data.notification.UserNotificationsNotifier
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
actual fun rememberNotificationPermissionRequest(): () -> Unit {
    val notifier = koinInject<UserNotificationsNotifier>()
    val scope = rememberCoroutineScope()
    return remember(notifier, scope) { { scope.launch { notifier.requestAuthorization() } } }
}
