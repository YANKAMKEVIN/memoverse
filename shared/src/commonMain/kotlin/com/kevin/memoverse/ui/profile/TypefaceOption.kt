package com.kevin.memoverse.ui.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kevin.memoverse.presentation.common.TypefaceUi
import com.kevin.memoverse.presentation.profile.TypefaceOptionUi
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.profile_typeface_sample
import com.kevin.memoverse.resources.typeface_cormorant_mood
import com.kevin.memoverse.resources.typeface_cormorant_name
import com.kevin.memoverse.resources.typeface_fraunces_mood
import com.kevin.memoverse.resources.typeface_fraunces_name
import com.kevin.memoverse.resources.typeface_garamond_mood
import com.kevin.memoverse.resources.typeface_garamond_name
import com.kevin.memoverse.resources.typeface_gloock_mood
import com.kevin.memoverse.resources.typeface_gloock_name
import com.kevin.memoverse.resources.typeface_jardin_mood
import com.kevin.memoverse.resources.typeface_jardin_name
import com.kevin.memoverse.ui.icons.AppIcons
import com.kevin.memoverse.ui.theme.LINING_FIGURES
import com.kevin.memoverse.ui.theme.Radius
import com.kevin.memoverse.ui.theme.Spacing
import com.kevin.memoverse.ui.theme.rememberAppFonts
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Une association de polices, dessinée dans ses propres polices pour qu'on la juge sur pièce. */
@Composable
fun TypefaceOption(
    option: TypefaceOptionUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val fonts = rememberAppFonts(option.typeface)
    val shape = RoundedCornerShape(Radius.card)
    val border by animateColorAsState(if (option.isSelected) colors.primary else colors.outline, label = "bordure")
    val (name, mood) = option.typeface.labels()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface, shape)
            .border(if (option.isSelected) 1.5.dp else 1.dp, border, shape)
            .selectable(selected = option.isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(Res.string.profile_typeface_sample),
                style = TextStyle(fontFeatureSettings = LINING_FIGURES, fontFamily = fonts.display, fontSize = (26 * fonts.displayScale).sp, color = colors.onSurface),
            )
            Text(
                text = stringResource(name),
                style = TextStyle(fontFeatureSettings = LINING_FIGURES, fontFamily = fonts.ui, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = colors.onSurface),
            )
            Text(
                text = stringResource(mood),
                style = TextStyle(fontFeatureSettings = LINING_FIGURES, fontFamily = fonts.ui, fontSize = 12.sp, color = colors.onSurfaceVariant),
            )
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(if (option.isSelected) colors.primary else Color.Transparent, CircleShape)
                .border(1.dp, if (option.isSelected) colors.primary else colors.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (option.isSelected) {
                Icon(AppIcons.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(14.dp))
            }
        }
    }
}

private fun TypefaceUi.labels(): Pair<StringResource, StringResource> = when (this) {
    TypefaceUi.JARDIN -> Res.string.typeface_jardin_name to Res.string.typeface_jardin_mood
    TypefaceUi.FRAUNCES -> Res.string.typeface_fraunces_name to Res.string.typeface_fraunces_mood
    TypefaceUi.CORMORANT -> Res.string.typeface_cormorant_name to Res.string.typeface_cormorant_mood
    TypefaceUi.GLOOCK -> Res.string.typeface_gloock_name to Res.string.typeface_gloock_mood
    TypefaceUi.GARAMOND -> Res.string.typeface_garamond_name to Res.string.typeface_garamond_mood
}
