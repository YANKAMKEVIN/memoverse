package com.kevin.memoverse.ui.permission

import androidx.compose.runtime.Composable

/** Ouvre la page de l'app dans les réglages du téléphone (après un refus du micro). */
@Composable
expect fun rememberAppSettingsOpener(): () -> Unit
