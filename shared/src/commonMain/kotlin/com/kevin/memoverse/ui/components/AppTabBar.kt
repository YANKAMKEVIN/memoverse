package com.kevin.memoverse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.tab_explorer
import com.kevin.memoverse.resources.tab_garden
import com.kevin.memoverse.resources.tab_profile
import com.kevin.memoverse.resources.tab_progression
import com.kevin.memoverse.ui.icons.AppIcons
import com.kevin.memoverse.ui.theme.InkMuted
import com.kevin.memoverse.ui.theme.Spacing
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class AppTab(val label: StringResource, val icon: ImageVector) {
    GARDEN(Res.string.tab_garden, AppIcons.Sprout),
    EXPLORER(Res.string.tab_explorer, AppIcons.Explore),
    PROGRESSION(Res.string.tab_progression, AppIcons.Progress),
    PROFILE(Res.string.tab_profile, AppIcons.Profile),
}

@Composable
fun AppTabBar(
    selected: AppTab,
    onSelect: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth().background(colors.surface)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.outline))
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AppTab.entries.forEach { tab ->
                val isSelected = tab == selected
                // Onglets inactifs : InkMuted (5,5:1 sur la crème) au lieu d'InkSubtle (2,5:1).
                val color = if (isSelected) colors.primary else InkMuted
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .semantics { role = Role.Tab; this.selected = isSelected }
                        .clickable { onSelect(tab) }
                        .padding(vertical = Spacing.xs),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    Icon(tab.icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                    Text(
                        text = stringResource(tab.label),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = color,
                        // Grande police : le libellé rétrécit au lieu de se couper en plein mot (« Progressio / n »).
                        maxLines = 1,
                        softWrap = false,
                        autoSize = TextAutoSize.StepBased(minFontSize = TAB_LABEL_MIN_SIZE, maxFontSize = MaterialTheme.typography.labelSmall.fontSize),
                    )
                }
            }
        }
    }
}

private val TAB_LABEL_MIN_SIZE = 8.sp
