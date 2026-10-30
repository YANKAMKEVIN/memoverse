package com.kevin.memoverse.ui.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.kevin.memoverse.data.notification.AuthorizationGatedNotifier
import com.kevin.memoverse.data.notification.UserNotificationsCenter
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenNotificationSettingsURLString

@Composable
actual fun rememberNotificationSettings(): NotificationSettingsAccess {
    val notifier = koinInject<AuthorizationGatedNotifier>()
    val center = remember { UserNotificationsCenter() }
    val scope = rememberCoroutineScope()
    var authorization by remember { mutableStateOf<Boolean?>(null) }
    var resumes by remember { mutableIntStateOf(0) }
    LifecycleResumeEffect(Unit) {
        resumes++
        onPauseOrDispose {}
    }
    LaunchedEffect(resumes) { authorization = center.authorization() }
    return NotificationSettingsAccess(isEnabled = authorization == true) {
        scope.launch {
            // Jamais demandé : iOS pose la question. Déjà répondu : seuls les réglages permettent d'en changer.
            if (center.authorization() == null) {
                notifier.requestAuthorization()
            } else {
                NSURL.URLWithString(UIApplicationOpenNotificationSettingsURLString)?.let {
                    UIApplication.sharedApplication.openURL(it, options = emptyMap<Any?, Any>(), completionHandler = null)
                }
            }
            authorization = center.authorization()
        }
    }
}
