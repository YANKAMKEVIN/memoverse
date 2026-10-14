package com.kevin.memoverse.presentation.session

import com.kevin.memoverse.domain.model.Exercise
import com.kevin.memoverse.domain.model.ExerciseAnswer
import com.kevin.memoverse.domain.model.HiddenPosition
import com.kevin.memoverse.domain.model.MasteryStage
import com.kevin.memoverse.domain.model.VerseParts
import com.kevin.memoverse.fake.learningVerse
import com.kevin.memoverse.fake.verse
import com.kevin.memoverse.presentation.ceremony.CeremonyUi
import com.kevin.memoverse.presentation.common.StageUi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionUiStateMapperTest {

    private val v1 = verse("v1", text = "un deux trois quatre")

    private fun feedbackFor(exercise: Exercise) =
        SessionModel(isLoading = false, exercises = listOf(exercise), lastResult = ExerciseResult(isCorrect = false)).toUiState().feedback!!

    @Test
    fun `la bonne réponse d'une référence est la référence`() {
        assertEquals("Référence v1", feedbackFor(Exercise.ChooseReference(v1, listOf("Référence v1", "Autre"))).expectedAnswer)
    }

    @Test
    fun `la bonne réponse d'une partie cachée est cette partie`() {
        val parts = VerseParts("un deux", "trois quatre", "", HiddenPosition.END)
        assertEquals("trois quatre", feedbackFor(Exercise.TypePart(v1, parts)).expectedAnswer)
    }

    @Test
    fun `réciter le verset n'a pas de réponse à part`() {
        assertNull(feedbackFor(Exercise.Recite(v1)).expectedAnswer)
    }

    @Test
    fun `le bilan compte l'XP - la précision et les versets qui ont grandi`() {
        val model = SessionModel(
            isLoading = false,
            exercises = listOf(Exercise.Recite(v1), Exercise.Recite(v1)),
            correctCount = 1,
            isFinished = true,
            verses = mapOf("v1" to learningVerse("v1", MasteryStage.ROOT)),
            initialStages = mapOf("v1" to MasteryStage.SEED),
        )

        val summary = model.toUiState().summary!!

        assertEquals(10, summary.xp)
        assertEquals(50, summary.accuracyPercent)
        assertEquals(listOf(GrownVerseUi("Référence v1", StageUi.SEED, StageUi.ROOT)), summary.grownVerses)
    }

    @Test
    fun `la précision est arrondie au plus proche`() {
        // Régression QA : 4 sur 6 affichaient 66 %.
        val model = SessionModel(isLoading = false, exercises = List(6) { Exercise.Recite(v1) }, correctCount = 4, isFinished = true)

        assertEquals(67, model.toUiState().summary!!.accuracyPercent)
    }

    @Test
    fun `le bilan signale les versets qui ont redescendu et encourage sous 50 pour cent`() {
        val model = SessionModel(
            isLoading = false,
            exercises = List(2) { Exercise.Recite(v1) },
            correctCount = 0,
            isFinished = true,
            verses = mapOf("v1" to learningVerse("v1", MasteryStage.ROOT)),
            initialStages = mapOf("v1" to MasteryStage.TREE),
        )

        val summary = model.toUiState().summary!!

        assertEquals(listOf(GrownVerseUi("Référence v1", StageUi.TREE, StageUi.ROOT)), summary.wiltedVerses)
        assertTrue(summary.isEncouragement)
    }

    @Test
    fun `après validation - la bonne proposition et le mauvais choix sont révélés`() {
        // Régression QA : le mauvais choix restait vert comme une bonne réponse.
        val exercise = Exercise.ChooseReference(v1, listOf("Autre", "Référence v1", "Encore"))
        val model = SessionModel(
            isLoading = false,
            exercises = listOf(exercise),
            draft = AnswerDraft.Choice("Autre"),
            lastResult = ExerciseResult(isCorrect = false),
        )

        val options = (model.toUiState().exercise as ExerciseUi.ChooseReference).options

        assertEquals(listOf(OptionVerdict.WRONG_CHOICE, OptionVerdict.CORRECT, null), options.map { it.verdict })
    }

    @Test
    fun `avant validation - aucune proposition n'est révélée`() {
        val exercise = Exercise.ChooseReference(v1, listOf("Autre", "Référence v1"))
        val model = SessionModel(isLoading = false, exercises = listOf(exercise), draft = AnswerDraft.Choice("Autre"))

        assertTrue((model.toUiState().exercise as ExerciseUi.ChooseReference).options.all { it.verdict == null })
    }

    @Test
    fun `une transcription vocale devient une réponse orale - une saisie une réponse écrite`() {
        val exercise = Exercise.ReciteAloud(v1)

        assertTrue(AnswerDraft.Typed("un deux", fromSpeech = true).toAnswer(exercise) is ExerciseAnswer.Spoken)
        assertTrue(AnswerDraft.Typed("un deux").toAnswer(exercise) is ExerciseAnswer.Typed)
    }

    @Test
    fun `les mots non dits sont cachés - puis révélés comme manqués après validation`() {
        val exercise = Exercise.ReciteAloud(verse("v1", text = "Or la foi est"))
        val draft = AnswerDraft.Typed("or la", fromSpeech = true)
        val listening = SessionModel(isLoading = false, exercises = listOf(exercise), draft = draft)

        val before = listening.toUiState().exercise as ExerciseUi.ReciteAloud
        assertEquals(
            listOf(RecitedWordState.RECITED, RecitedWordState.RECITED, RecitedWordState.HIDDEN, RecitedWordState.HIDDEN),
            before.words.map { it.state },
        )
        assertEquals(listOf("Or", "la", "foi", "est"), before.words.map { it.text })

        val after = listening.copy(lastResult = ExerciseResult(isCorrect = false)).toUiState().exercise as ExerciseUi.ReciteAloud
        assertEquals(RecitedWordState.MISSED, after.words[2].state)
    }

    private fun finished(
        initial: Map<String, MasteryStage>,
        current: Map<String, MasteryStage>,
        watered: Set<String> = emptySet(),
        isCeremonyDone: Boolean = false,
    ) = SessionModel(
        isLoading = false,
        exercises = initial.keys.map { Exercise.Recite(verse(it)) },
        correctCount = watered.size,
        isFinished = true,
        verses = current.mapValues { (id, stage) -> learningVerse(id, stage) },
        initialStages = initial,
        wateredVerseIds = watered,
        isCeremonyDone = isCeremonyDone,
    )

    @Test
    fun `la scène d'arrosage met en valeur le verset qui a grandi le plus haut`() {
        val model = finished(
            initial = mapOf("v1" to MasteryStage.SEED, "v2" to MasteryStage.TREE, "v3" to MasteryStage.ROOT),
            current = mapOf("v1" to MasteryStage.ROOT, "v2" to MasteryStage.FRUIT, "v3" to MasteryStage.ROOT),
            watered = setOf("v1", "v2", "v3"),
        )

        assertEquals(CeremonyUi.Watering("Référence v2", StageUi.TREE, StageUi.FRUIT, otherGrownCount = 1), model.toUiState().ceremony)
    }

    @Test
    fun `sans croissance la scène montre le verset réussi le plus grand à son stade`() {
        val model = finished(
            initial = mapOf("v1" to MasteryStage.ROOT, "v2" to MasteryStage.TREE, "v3" to MasteryStage.FRUIT),
            current = mapOf("v1" to MasteryStage.ROOT, "v2" to MasteryStage.TREE, "v3" to MasteryStage.TREE),
            watered = setOf("v1", "v2"),
        )

        val ceremony = assertIs<CeremonyUi.Watering>(model.toUiState().ceremony)

        assertEquals(CeremonyUi.Watering("Référence v2", StageUi.TREE, StageUi.TREE), ceremony)
        assertFalse(ceremony.hasGrown)
    }

    @Test
    fun `un verset qui fane sans aucune réussite joue la variante sobre`() {
        val model = finished(initial = mapOf("v1" to MasteryStage.TREE), current = mapOf("v1" to MasteryStage.ROOT))

        assertEquals(CeremonyUi.Wilting("Référence v1", StageUi.TREE, StageUi.ROOT), model.toUiState().ceremony)
    }

    @Test
    fun `une séance qui ne change rien ne joue aucune scène`() {
        val model = finished(initial = mapOf("v1" to MasteryStage.SEED), current = mapOf("v1" to MasteryStage.SEED))

        val state = model.toUiState()

        assertNull(state.ceremony)
        assertNotNull(state.summary)
    }

    @Test
    fun `une fois la scène jouée seul le bilan reste`() {
        val model = finished(
            initial = mapOf("v1" to MasteryStage.SEED),
            current = mapOf("v1" to MasteryStage.ROOT),
            watered = setOf("v1"),
            isCeremonyDone = true,
        )

        val state = model.toUiState()

        assertNull(state.ceremony)
        assertEquals(listOf(GrownVerseUi("Référence v1", StageUi.SEED, StageUi.ROOT)), state.summary!!.grownVerses)
    }
}
