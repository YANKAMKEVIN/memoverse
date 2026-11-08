package com.kevin.memoverse.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kevin.memoverse.presentation.profile.ProfileEvent
import com.kevin.memoverse.presentation.profile.ProfileUiState
import com.kevin.memoverse.presentation.profile.ProfileViewModel
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.profile_offline
import com.kevin.memoverse.resources.profile_reminders_enable
import com.kevin.memoverse.resources.profile_reminders_manage
import com.kevin.memoverse.resources.profile_reminders_off
import com.kevin.memoverse.resources.profile_reminders_on
import com.kevin.memoverse.resources.profile_reminders_title
import com.kevin.memoverse.resources.profile_reset
import com.kevin.memoverse.resources.profile_reset_cancel
import com.kevin.memoverse.resources.profile_reset_confirm
import com.kevin.memoverse.resources.profile_reset_done
import com.kevin.memoverse.resources.profile_reset_message
import com.kevin.memoverse.resources.profile_reset_title
import com.kevin.memoverse.resources.profile_title
import com.kevin.memoverse.resources.profile_translation_label
import com.kevin.memoverse.resources.profile_translation_value
import com.kevin.memoverse.resources.profile_version_label
import com.kevin.memoverse.resources.profile_version_value
import com.kevin.memoverse.resources.start_error_save_failed
import com.kevin.memoverse.ui.components.Wordmark
import com.kevin.memoverse.ui.components.textAction
import com.kevin.memoverse.ui.permission.rememberNotificationSettings
import com.kevin.memoverse.ui.theme.AppTheme
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Profil V1 : informations sur l'app et réinitialisation du jardin (pas de compte en V1). */
@Composable
fun ProfileScreen(
    contentPadding: PaddingValues,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val notifications = rememberNotificationSettings()
    ProfileScreenContent(
        state = state,
        contentPadding = contentPadding,
        onEvent = viewModel::onEvent,
        remindersEnabled = notifications.isEnabled,
        onRemindersClick = notifications.open,
    )
}

@Composable
fun ProfileScreenContent(
    state: ProfileUiState,
    contentPadding: PaddingValues,
    onEvent: (ProfileEvent) -> Unit,
    remindersEnabled: Boolean = false,
    onRemindersClick: () -> Unit = {},
) {
    val shape = RoundedCornerShape(Radius.card)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = contentPadding.calculateBottomPadding())
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Wordmark()
        Text(stringResource(Res.string.profile_title), style = MaterialTheme.typography.displayMedium, modifier = Modifier.padding(top = Spacing.sm))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, shape)
                .border(1.dp, MaterialTheme.colorScheme.outline, shape)
                .padding(horizontal = Spacing.lg),
        ) {
            InfoRow(Res.string.profile_translation_label, Res.string.profile_translation_value)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            InfoRow(Res.string.profile_version_label, Res.string.profile_version_value)
        }
        RemindersRow(isEnabled = remindersEnabled, onClick = onRemindersClick, shape = shape)
        Text(
            text = stringResource(Res.string.profile_offline),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.profile_reset),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.textAction { onEvent(ProfileEvent.ResetClicked) }.padding(horizontal = Spacing.md),
        )
        when {
            state.isResetDone -> Text(stringResource(Res.string.profile_reset_done), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            state.hasResetFailed -> Text(stringResource(Res.string.start_error_save_failed), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
    if (state.isConfirmingReset) {
        AlertDialog(
            onDismissRequest = { onEvent(ProfileEvent.ResetCancelled) },
            title = { Text(stringResource(Res.string.profile_reset_title)) },
            text = { Text(stringResource(Res.string.profile_reset_message)) },
            confirmButton = {
                TextButton(onClick = { onEvent(ProfileEvent.ResetConfirmed) }) {
                    Text(stringResource(Res.string.profile_reset_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(ProfileEvent.ResetCancelled) }) { Text(stringResource(Res.string.profile_reset_cancel)) }
            },
        )
    }
}

@Composable
private fun RemindersRow(isEnabled: Boolean, onClick: () -> Unit, shape: Shape) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(start = Spacing.lg, end = Spacing.sm, top = Spacing.md, bottom = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            Text(stringResource(Res.string.profile_reminders_title), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(if (isEnabled) Res.string.profile_reminders_on else Res.string.profile_reminders_off),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = onClick) {
            Text(stringResource(if (isEnabled) Res.string.profile_reminders_manage else Res.string.profile_reminders_enable))
        }
    }
}

@Composable
private fun InfoRow(label: StringResource, value: StringResource) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.lg), horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Text(stringResource(label), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(value), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    AppTheme { ProfileScreenContent(state = ProfileUiState(), contentPadding = PaddingValues(), onEvent = {}) }
}

@Preview
@Composable
private fun ProfileScreenConfirmPreview() {
    AppTheme { ProfileScreenContent(state = ProfileUiState(isConfirmingReset = true), contentPadding = PaddingValues(), onEvent = {}) }
}
