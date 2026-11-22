package com.kevin.memoverse.presentation.session

import com.kevin.memoverse.domain.model.Exercise
import com.kevin.memoverse.domain.model.HiddenPosition
import com.kevin.memoverse.domain.model.MasteryStage
import com.kevin.memoverse.domain.model.VerseParts
import com.kevin.memoverse.domain.model.WateringStats
import com.kevin.memoverse.fake.learningVerse
import com.kevin.memoverse.fake.verse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SessionSnapshotCodecTest {

    private val v1 = verse("v1", text = "Car Dieu a tant aimé le monde")
    private val parts = VerseParts(before = "Car Dieu", hidden = "a tant aimé", after = "le monde", position = HiddenPosition.MIDDLE)
    private val allExercises = listOf(
        Exercise.ChooseReference(v1, listOf("Référence v1", "Jean 1 1")),
        Exercise.Reorder(v1, listOf("Car Dieu", "a tant aimé", "le monde"), listOf(2, 0, 1)),
        Exercise.FillBlanks(v1, listOf("Car", "Dieu", "a", "tant"), listOf(1, 3), listOf("tant", "Dieu", "peu")),
        Exercise.TypeBlanks(v1, listOf("Car", "Dieu", "a", "tant"), listOf(0)),
        Exercise.CompletePart(v1, parts, listOf("a tant aimé", "a créé")),
        Exercise.TypePart(v1, parts),
        Exercise.Recite(v1),
        Exercise.ReciteAloud(v1),
    )

    private fun session(draft: AnswerDraft? = null) = SessionModel(
        isLoading = false,
        exercises = allExercises,
        index = 7,
        draft = draft,
        lastResult = ExerciseResult(isCorrect = true, grewTo = MasteryStage.ROOT, notSaved = true),
        verses = mapOf("v1" to learningVerse("v1").copy(successCount = 2, lastSuccessEpochDay = 19_999, dueAtEpochMillis = 42)),
        initialStages = mapOf("v1" to MasteryStage.SEED),
        correctCount = 5,
        wateredVerseIds = linkedSetOf("v1", "v3", "v2"),
        speech = SpeechModel(isTyping = true, committedText = "Car Dieu", typedText = "a tant"),
        isConfirmingQuit = true,
        stats = WateringStats(totalXp = 420, streakDays = 3, lastSessionEpochDay = 19_999),
    )

    private fun roundTrip(model: SessionModel): SessionModel? = SessionSnapshotCodec.decode(SessionSnapshotCodec.encode(model))

    @Test
    fun `une séance avec les 8 types d'exercices se retrouve à l'identique`() {
        val model = session(AnswerDraft.Typed(text = "Car Dieu a", fromSpeech = true))
        assertEquals(model, roundTrip(model))
    }

    @Test
    fun `chaque forme de réponse en cours est retrouvée`() {
        listOf(
            AnswerDraft.Choice("Référence v1"),
            AnswerDraft.Choice(),
            AnswerDraft.Order(listOf(2, 0)),
            AnswerDraft.Blanks(listOf(1, null)),
            AnswerDraft.Typed("a tant"),
            AnswerDraft.TypedBlanks(listOf("Car", "")),
        ).forEach { draft -> assertEquals(draft, roundTrip(session(draft))?.draft) }
    }

    @Test
    fun `l'ordre des versets arrosés est gardé`() {
        assertEquals(listOf("v1", "v3", "v2"), roundTrip(session())?.wateredVerseIds?.toList())
    }

    @Test
    fun `le micro repart éteint et sans erreur`() {
        val listening = session().copy(speech = SpeechModel(isListening = true, committedText = "Car", error = SpeechErrorUi.NO_MATCH))
        val speech = assertNotNull(roundTrip(listening)).speech
        assertFalse(speech.isListening)
        assertNull(speech.error)
        assertEquals("Car", speech.committedText)
    }

    @Test
    fun `rien n'est sauvegardé pendant le chargement ni après une erreur ou une sortie`() {
        assertNull(SessionSnapshotCodec.encode(SessionModel(isLoading = true)))
        assertNull(SessionSnapshotCodec.encode(SessionModel(isLoading = false, hasError = true)))
        assertNull(SessionSnapshotCodec.encode(SessionModel(isLoading = false)))
        assertNull(SessionSnapshotCodec.encode(session().copy(shouldClose = true)))
    }

    @Test
    fun `une séance terminée reste au bilan`() {
        val finished = session().copy(index = 7, lastResult = null, isFinished = true, isCeremonyDone = true)
        assertEquals(finished, roundTrip(finished))
    }

    @Test
    fun `une sauvegarde absente abîmée ou incohérente est ignorée`() {
        assertNull(SessionSnapshotCodec.decode(null))
        assertNull(SessionSnapshotCodec.decode(""))
        assertNull(SessionSnapshotCodec.decode("{\"exercises\": 3}"))
        val encoded = assertNotNull(SessionSnapshotCodec.encode(session()))
        assertNull(SessionSnapshotCodec.decode(encoded.replace("\"index\":7", "\"index\":12")))
    }
}
