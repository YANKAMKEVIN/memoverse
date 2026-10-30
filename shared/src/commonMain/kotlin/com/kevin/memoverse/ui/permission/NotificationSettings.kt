package com.kevin.memoverse.ui.permission

import androidx.compose.runtime.Composable

/** Rappels autorisés ou non par le système, et le geste qui permet de les (ré)activer. */
class NotificationSettingsAccess(val isEnabled: Boolean, val open: () -> Unit)

/**
 * Pour la ligne « Rappels » du Profil : sans elle, un refus ne pouvait plus être rattrapé.
 * L'état est relu à chaque retour dans l'app (la personne a pu changer d'avis dans les réglages).
 */
@Composable
expect fun rememberNotificationSettings(): NotificationSettingsAccess
