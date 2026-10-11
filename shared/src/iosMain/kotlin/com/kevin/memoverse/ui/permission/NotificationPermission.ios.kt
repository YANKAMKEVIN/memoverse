package com.kevin.memoverse.ui.permission

import androidx.compose.runtime.Composable

/** iOS demande l'autorisation au premier rappel programmé (UserNotificationsNotifier). */
@Composable
actual fun rememberNotificationPermissionRequest(): () -> Unit = {}
