package com.kevin.memoverse.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kevin.memoverse.presentation.common.StageUi
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.onboarding_grow_body
import com.kevin.memoverse.resources.onboarding_grow_title
import com.kevin.memoverse.resources.onboarding_next
import com.kevin.memoverse.resources.onboarding_page
import com.kevin.memoverse.resources.onboarding_plant_body
import com.kevin.memoverse.resources.onboarding_plant_title
import com.kevin.memoverse.resources.onboarding_skip
import com.kevin.memoverse.resources.onboarding_start
import com.kevin.memoverse.resources.onboarding_water_body
import com.kevin.memoverse.resources.onboarding_water_title
import com.kevin.memoverse.resources.scene_premier_lancement
import com.kevin.memoverse.ui.components.PillButton
import com.kevin.memoverse.ui.components.PlantImage
import com.kevin.memoverse.ui.components.Wordmark
import com.kevin.memoverse.ui.components.stageName
import com.kevin.memoverse.ui.session.exercises.WaterDrop
import com.kevin.memoverse.ui.theme.AppTheme
import com.kevin.memoverse.ui.theme.Sand
import com.kevin.memoverse.ui.theme.Spacing
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Accueil du premier lancement, en trois volets passables : planter, arroser, voir grandir.
 * Affiché une seule fois, avant le jardin vide.
 */
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pages = OnboardingPage.entries
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.lastIndex
    Column(modifier = Modifier.fillMaxSize().background(Sand)) {
        Row(
            // Hauteur fixe : la barre ne bouge pas quand « Passer » disparaît au dernier volet.
            modifier = Modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 48.dp).padding(start = Spacing.xl, end = Spacing.sm, top = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Wordmark(modifier = Modifier.weight(1f))
            if (!isLast) TextButton(onClick = onFinished) { Text(stringResource(Res.string.onboarding_skip), style = MaterialTheme.typography.titleMedium) }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
            OnboardingPageContent(pages[index])
        }
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = Spacing.xxl, vertical = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            val pageLabel = stringResource(Res.string.onboarding_page, pagerState.currentPage + 1, pages.size)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = pageLabel }) {
                repeat(pages.size) { index ->
                    Box(
                        Modifier
                            .size(if (index == pagerState.currentPage) 10.dp else 8.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = if (index == pagerState.currentPage) 1f else 0.25f), CircleShape),
                    )
                }
            }
            PillButton(
                text = stringResource(if (isLast) Res.string.onboarding_start else Res.string.onboarding_next),
                onClick = { if (isLast) onFinished() else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private enum class OnboardingPage(val title: StringResource, val body: StringResource) {
    PLANT(Res.string.onboarding_plant_title, Res.string.onboarding_plant_body),
    WATER(Res.string.onboarding_water_title, Res.string.onboarding_water_body),
    GROW(Res.string.onboarding_grow_title, Res.string.onboarding_grow_body),
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when (page) {
                OnboardingPage.PLANT -> {
                    Image(
                        painter = painterResource(Res.drawable.scene_premier_lancement),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    // Fondu vers le sable : le texte se pose sur un fond calme.
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.7f to Sand.copy(alpha = 0f), 1f to Sand)))
                }
                OnboardingPage.WATER -> WaterDrop(fill = WATER_DEMO_FILL, isListening = true, enabled = false, label = "", onClick = {})
                OnboardingPage.GROW -> Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.Bottom) {
                    StageUi.entries.forEach { stage ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            PlantImage(stage = stage, scale = GROW_PLANT_SCALE)
                            Text(stageName(stage), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.xxl, vertical = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Text(stringResource(page.title), style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
            Text(
                text = stringResource(page.body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private const val WATER_DEMO_FILL = 0.6f
private const val GROW_PLANT_SCALE = 0.44f

@Preview
@Composable
private fun OnboardingScreenPreview() {
    AppTheme { OnboardingScreen(onFinished = {}) }
}
