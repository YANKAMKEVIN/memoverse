package com.kevin.memoverse.ui.permission

import androidx.compose.runtime.Composable

/**
 * Renvoie une action qui demande l'autorisation d'envoyer des rappels, appelée au moment où elle a
 * du sens (après la première plantation). Sur iOS, la demande est faite au premier rappel programmé.
 */
@Composable
expect fun rememberNotificationPermissionRequest(): () -> Unit
