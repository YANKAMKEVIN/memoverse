package com.kevin.memoverse

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.kevin.memoverse.domain.exercise.ExerciseFactory
import com.kevin.memoverse.domain.repository.LearningRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import kotlin.random.Random
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.kevin.memoverse.domain.model.MasteryStage
import kotlinx.coroutines.flow.first

/**
 * Tests d'acceptance : parcours complets dans la vraie app (à lancer sur le S21+ de QA).
 * Chaque test part d'un jardin vide et d'exercices tirés avec un hasard fixe.
 */
@RunWith(AndroidJUnit4::class)
class GardenJourneyTest {

    @get:Rule(order = 0)
    val permissions: GrantPermissionRule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.RECORD_AUDIO)
    } else {
        GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)
    }

    @get:Rule(order = 1)
    val compose = createAndroidComposeRule<MainActivity>()

    private val robot = MemoverseRobot(compose)

    private val foi = listOf("Hébreux 11:1", "Hébreux 11:6", "Romains 10:17")

    @Before
    fun startWithEmptyGarden() {
        // Le téléphone de QA peut être verrouillé : l'app s'affiche par-dessus, sans le déverrouiller.
        compose.activityRule.scenario.onActivity {
            it.setShowWhenLocked(true)
            it.setTurnScreenOn(true)
        }
        loadKoinModules(module { factory { ExerciseFactory(Random(SEED), isSpeechAvailable = false) } })
        clearGarden()
        robot.waitForText("Ton jardin attend")
    }

    @After
    fun cleanUp() {
        clearGarden()
    }

    private fun clearGarden() {
        runBlocking { KoinPlatform.getKoin().get<LearningRepository>().clear() }
    }

    @Test
    fun planter_puis_revenir_sur_explorer_permet_de_choisir_un_autre_theme() {
        robot.tap("Planter mes premiers versets")
        robot.plantInTheme("Foi", *foi.toTypedArray())
        robot.waitForText("Arroser · 3 versets ont soif")

        robot.tapTab("Explorer")
        robot.openThemeFromExplorer("Paix")

        assertTrue(robot.isDisplayed("Paix"))
        assertTrue(robot.isDisplayed("Jean 14:27"))
    }

    @Test
    fun un_jardin_plein_est_annonce_des_l_ouverture_d_un_theme() {
        robot.tap("Planter mes premiers versets")
        robot.plantInTheme("Foi", *foi.toTypedArray())
        robot.waitForText("Arroser · 3 versets ont soif")

        robot.tapTab("Explorer")
        robot.openThemeFromExplorer("Paix")

        robot.waitForText("Ton jardin a déjà 3 versets en cours sur 5", substring = true)
        // Jardin plein : à la place d'un « Planter » grisé, un raccourci vers l'arrosage.
        robot.waitForText("Jardin plein")
        robot.tap("Aller arroser")
        robot.waitForText("Arroser · 3 versets ont soif")
    }

    @Test
    fun le_jardin_affiche_une_etiquette_par_verset_plante() {
        // Régression QA : seules 3 étiquettes s'affichaient.
        robot.tap("Planter mes premiers versets")
        robot.plantInTheme("Paix", "Jean 14:27", "Ésaïe 26:3", "Matthieu 11:28", "Colossiens 3:15")

        robot.waitForText("Arroser · 4 versets ont soif")
        listOf("Jean 14:27", "Ésaïe 26:3", "Matthieu 11:28", "Colossiens 3:15").forEach {
            assertTrue("étiquette manquante : $it", robot.isDisplayed("$it · a soif"))
        }
    }

    @Test
    fun la_reference_n_est_pas_affichee_avant_la_reponse() {
        // Régression QA : « Retrouve la référence » affichait la référence en titre.
        robot.tap("Planter mes premiers versets")
        robot.plantInTheme("Foi", *foi.toTypedArray())
        robot.tap("Arroser · 3 versets ont soif")

        robot.waitForText("Retrouve la référence")
        foi.forEach { assertFalse("référence visible : $it", robot.isDisplayed(it.uppercase())) }
    }

    @Test
    fun quitter_une_seance_commencee_demande_confirmation() {
        // Régression QA : quitter en pleine séance ne demandait rien.
        robot.tap("Planter mes premiers versets")
        robot.plantInTheme("Foi", *foi.toTypedArray())
        robot.tap("Arroser · 3 versets ont soif")
        val planted = robot.verses("hebreux-11-1", "hebreux-11-6", "romains-10-17")
        robot.answerCorrectly(planted)
        robot.validate()
        robot.continueAfterFeedback()

        robot.pressBack()
        robot.waitForText("Quitter l’arrosage ?")
        robot.tap("Continuer l’arrosage")
        robot.waitForText("Valider")

        robot.pressBack()
        robot.tap("Quitter")
        robot.waitForText("en croissance")
    }

    @Test
    fun avec_le_clavier_ouvert_le_champ_et_valider_restent_visibles() {
        // Régression QA : « Valider » flottait par-dessus le champ de saisie.
        robot.tap("Planter mes premiers versets")
        robot.plantInTheme("Foi", *foi.toTypedArray())
        robot.waitForText("Arroser · 3 versets ont soif")
        // Stade Racine : le premier exercice fait écrire les mots manquants.
        runBlocking {
            val repository = KoinPlatform.getKoin().get<LearningRepository>()
            repository.observeLearningVerses().first().forEach { repository.updateVerse(it.copy(stage = MasteryStage.ROOT)) }
        }
        robot.tap("Arroser · 3 versets ont soif")
        robot.waitForText("Écris les mots manquants")

        val firstBlank = compose.onAllNodes(hasSetTextAction()).onFirst()
        firstBlank.performClick()
        firstBlank.performTextInput("mot")
        compose.waitForIdle()

        firstBlank.assertIsDisplayed()
        compose.onNode(hasText("Valider")).assertIsDisplayed()
        val field = firstBlank.fetchSemanticsNode().boundsInRoot
        val button = compose.onNode(hasText("Valider")).fetchSemanticsNode().boundsInRoot
        assertTrue("le bouton recouvre le champ", field.bottom <= button.top)
    }

    @Test
    fun une_seance_reussie_fait_grandir_le_jardin_et_programme_le_prochain_arrosage() {
        robot.tap("Planter mes premiers versets")
        robot.plantInTheme("Foi", *foi.toTypedArray())
        robot.tap("Arroser · 3 versets ont soif")

        val planted = robot.verses("hebreux-11-1", "hebreux-11-6", "romains-10-17")
        repeat(EXERCISES_FOR_THREE_SEEDS) {
            robot.waitForText("Valider")
            robot.answerCorrectly(planted)
            robot.validate()
            robot.waitForText("Juste.")
            robot.continueAfterFeedback()
        }

        robot.waitForText("Ton jardin est arrosé")
        assertTrue(robot.isDisplayed("+60 XP"))
        assertTrue(robot.isDisplayed("100 %"))
        robot.tap("Continuer")
        robot.waitForText("prochain arrosage dans 6 h", substring = true)
    }

    @Test
    fun une_mauvaise_reponse_montre_la_bonne() {
        robot.tap("Planter mes premiers versets")
        robot.plantInTheme("Foi", *foi.toTypedArray())
        robot.tap("Arroser · 3 versets ont soif")

        robot.waitForText("Retrouve la référence")
        robot.answerWrongReference(robot.verses("hebreux-11-1", "hebreux-11-6", "romains-10-17"))
        robot.validate()

        robot.waitForText("Presque.")
        assertTrue(robot.isDisplayed("La bonne réponse :"))
    }

    @Test
    fun la_progression_compte_les_graines_et_le_profil_peut_vider_le_jardin() {
        robot.tap("Planter mes premiers versets")
        robot.plantInTheme("Foi", *foi.toTypedArray())
        robot.waitForText("Arroser · 3 versets ont soif")

        robot.tapTab("Progression")
        robot.waitForText("3 versets")

        robot.tapTab("Profil")
        robot.tap("Réinitialiser mon jardin")
        robot.tap("Réinitialiser")
        robot.waitForText("Ton jardin est vide", substring = true)

        robot.tapTab("Jardin")
        robot.waitForText("Ton jardin attend")
    }

    private companion object {
        const val SEED = 7
        /** 3 graines × 2 exercices (référence puis remise en ordre). */
        const val EXERCISES_FOR_THREE_SEEDS = 6
    }
}
