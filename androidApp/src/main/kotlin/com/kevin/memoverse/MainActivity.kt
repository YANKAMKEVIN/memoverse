package com.kevin.memoverse

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kevin.memoverse.data.notification.NotificationEntry
import com.kevin.memoverse.data.notification.ReviewNotificationWorker

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // L'UI est claire uniquement pour l'instant : icônes sombres même si le téléphone est en mode sombre.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        handleNotificationTap(intent)
        setContent { App() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationTap(intent)
    }

    private fun handleNotificationTap(intent: Intent?) {
        if (intent?.getBooleanExtra(ReviewNotificationWorker.EXTRA_OPEN_SESSION, false) == true) {
            intent.removeExtra(ReviewNotificationWorker.EXTRA_OPEN_SESSION)
            NotificationEntry.requestSessionOpen()
        }
    }
}
