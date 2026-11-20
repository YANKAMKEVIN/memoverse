package com.kevin.memoverse.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.loading
import com.kevin.memoverse.ui.icons.AppIcons
import org.jetbrains.compose.resources.stringResource

/** Attente aux couleurs du jardin : une pousse qui respire, plutôt qu'un cercle Material. */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "pousse")
    val breath by transition.animateFloat(
        initialValue = BREATH_MIN,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(BREATH_MILLIS), RepeatMode.Reverse),
        label = "souffle",
    )
    val label = stringResource(Res.string.loading)
    Box(modifier = modifier.fillMaxSize().semantics { contentDescription = label }, contentAlignment = Alignment.Center) {
        Icon(
            imageVector = AppIcons.Sprout,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(56.dp)
                .graphicsLayer {
                    // La pousse grandit depuis le sol et s'éclaircit en expirant.
                    scaleX = breath
                    scaleY = breath
                    alpha = 0.45f + 0.55f * breath
                    transformOrigin = TransformOrigin(0.5f, 1f)
                },
        )
    }
}

private const val BREATH_MIN = 0.82f
private const val BREATH_MILLIS = 1100
