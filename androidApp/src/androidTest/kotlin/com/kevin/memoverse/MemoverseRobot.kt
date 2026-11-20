package com.kevin.memoverse

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.Verse
import com.kevin.memoverse.domain.repository.VerseBankRepository
import kotlinx.coroutines.runBlocking
import org.koin.mp.KoinPlatform

/** Actions d'un utilisateur dans l'app, réutilisées par les tests d'acceptance. */
class MemoverseRobot(private val rule: ComposeTestRule) {

    fun waitForText(text: String, substring: Boolean = false, timeoutMillis: Long = TIMEOUT_MILLIS) {
        rule.waitUntil(timeoutMillis) { rule.onAllNodesWithText(text, substring = substring).fetchSemanticsNodes().isNotEmpty() }
    }

    fun tap(text: String, substring: Boolean = false) {
        waitForText(text, substring)
        // Un élément sous le bas de l'écran (Profil qui défile) doit d'abord être amené à l'écran.
        val node = rule.onAllNodesWithText(text, substring = substring).filterToOne(hasClickAction())
        if (rule.onAllNodes(hasText(text, substring = substring) and hasClickAction() and hasAnyAncestor(hasScrollAction())).fetchSemanticsNodes().isNotEmpty()) {
            node.performScrollTo()
        }
        node.performClick()
        rule.waitForIdle()
    }

    /** Au tout premier lancement, l'accueil en trois volets passe avant le jardin : on le passe. */
    fun skipOnboardingIfShown() {
        rule.waitUntil(TIMEOUT_MILLIS) { isDisplayed(ONBOARDING_SKIP) || isDisplayed("MEMOVERSE") }
        if (isDisplayed(ONBOARDING_SKIP)) tap(ONBOARDING_SKIP)
    }

    /** Les onglets fusionnent icône et libellé dans un seul nœud cliquable. */
    fun tapTab(label: String) = tap(label)

    fun openThemeFromExplorer(theme: String) {
        waitForText(EXPLORER_SUBTITLE, substring = true)
        rule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(theme) and hasClickAction())
        rule.onNode(hasText(theme) and hasClickAction()).performClick()
        // Selon la place restante, l'écran affiche la consigne ou le bandeau « jardin plein ».
        rule.waitUntil(TIMEOUT_MILLIS) { isDisplayed(THEME_INSTRUCTIONS) || isDisplayed(GARDEN_FULL, substring = true) }
    }

    fun selectVerses(vararg references: String) {
        references.forEach { reference ->
            rule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(reference))
            rule.onNodeWithText(reference).performClick()
        }
        rule.waitForIdle()
    }

    fun plant() {
        rule.onNode(hasText("Planter") and hasClickAction()).performClick()
        // L'horloge de test avance d'elle-même : la scène de plantation se joue jusqu'au bout, puis le jardin s'ouvre.
        rule.waitForIdle()
    }

    fun plantInTheme(theme: String, vararg references: String) {
        openThemeFromExplorer(theme)
        selectVerses(*references)
        plant()
    }

    fun pressBack() {
        androidx.test.espresso.Espresso.pressBack()
        rule.waitForIdle()
    }

    fun isDisplayed(text: String, substring: Boolean = false): Boolean =
        rule.onAllNodesWithText(text, substring = substring).fetchSemanticsNodes().isNotEmpty()

    fun validate() {
        rule.waitUntil(TIMEOUT_MILLIS) { rule.onAllNodes(hasText("Valider") and isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        rule.onNode(hasText("Valider") and isEnabled()).performClick()
        rule.waitForIdle()
    }

    /** Après le dernier exercice, la scène d'arrosage se joue jusqu'au bout (horloge de test) avant le bilan. */
    fun continueAfterFeedback() {
        tap("Continuer")
    }

    /** Verset de l'exercice affiché : par sa référence en titre, ou par son texte (« Retrouve la référence »). */
    fun currentVerse(planted: List<Verse>): Verse =
        planted.firstOrNull { isDisplayed(it.reference.uppercase()) } ?: planted.first { isDisplayed(it.text, substring = true) }

    /** Répond juste à l'exercice affiché (référence ou remise en ordre), d'après le vrai texte du verset. */
    fun answerCorrectly(planted: List<Verse>) {
        val verse = currentVerse(planted)
        when {
            isDisplayed("Retrouve la référence") -> rule.onNode(hasText(verse.reference) and hasClickAction()).performClick()
            isDisplayed("Remets les mots dans l’ordre") -> placeChunksInOrder(verse.text)
            else -> error("Exercice inattendu à l'écran")
        }
        rule.waitForIdle()
    }

    /** Répond faux : une autre référence que celle du verset. */
    fun answerWrongReference(planted: List<Verse>) {
        val verse = currentVerse(planted)
        val options = rule.onAllNodes(hasClickAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).fetchSemanticsNodes()
        val wrong = options.map { it.config[SemanticsProperties.Text].joinToString { t -> t.text } }.first { it != verse.reference }
        rule.onNode(hasText(wrong) and hasClickAction()).performClick()
        rule.waitForIdle()
    }

    private fun placeChunksInOrder(verseText: String) {
        var remaining = verseText
        while (remaining.isNotBlank()) {
            val candidates = rule.onAllNodes(hasClickAction() and isEnabled()).fetchSemanticsNodes()
                .mapNotNull { node -> node.config.getOrNull(SemanticsProperties.Text)?.joinToString("") { it.text } }
            val chunk = candidates.filter { remaining.startsWith(it) }.maxByOrNull { it.length }
                ?: error("Aucun galet ne commence « $remaining »")
            rule.onAllNodes(hasText(chunk) and hasClickAction()).onLast().performClick()
            rule.waitForIdle()
            remaining = remaining.removePrefix(chunk).trimStart()
        }
    }

    fun verses(vararg ids: String): List<Verse> = runBlocking {
        val result = KoinPlatform.getKoin().get<VerseBankRepository>().getVerses(ids.toList())
        (result as DataResult.Success).data
    }

    companion object {
        const val TIMEOUT_MILLIS = 10_000L
        const val EXPLORER_SUBTITLE = "thèmes pour faire grandir ta foi"
        const val THEME_INSTRUCTIONS = "Choisis 3 ou 4 versets à planter."
        const val GARDEN_FULL = "Ton jardin a déjà"
        const val ONBOARDING_SKIP = "Passer"
    }
}
