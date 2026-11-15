package com.kevin.memoverse.presentation.session

import app.cash.turbine.test
import com.kevin.memoverse.domain.exercise.ExerciseFactory
import com.kevin.memoverse.domain.model.MasteryStage
import com.kevin.memoverse.domain.usecase.BuildWateringSessionUseCase
import com.kevin.memoverse.domain.usecase.CheckAnswerUseCase
import com.kevin.memoverse.domain.speech.SpeechError
import com.kevin.memoverse.domain.speech.SpeechEvent
import com.kevin.memoverse.domain.usecase.ListenToRecitationUseCase
import com.kevin.memoverse.domain.usecase.MeasureRecitationUseCase
import com.kevin.memoverse.domain.usecase.RecordExerciseResultUseCase
import com.kevin.memoverse.fake.FakeLearningRepository
import com.kevin.memoverse.fake.FakeSpeechRecognitionService
import com.kevin.memoverse.fake.FakeVerseBankRepository
import com.kevin.memoverse.fake.learningVerse
import com.kevin.memoverse.fake.verse
import com.kevin.memoverse.presentation.ceremony.CeremonyUi
import com.kevin.memoverse.presentation.common.StageUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val bank = FakeVerseBankRepository(verses = (1..6).map { verse("v$it", text = "mot$it alpha beta gamma delta epsilon zeta eta theta") })

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val speech = FakeSpeechRecognitionService()

    private fun viewModel(learning: FakeLearningRepository) = SessionViewModel(
        buildSession = BuildWateringSessionUseCase(learning, bank, ExerciseFactory(Random(3))) { 20_000L * 86_400_000L },
        checkAnswer = CheckAnswerUseCase(),
        recordResult = RecordExerciseResultUseCase(learning, { 20_000L * 86_400_000L }),
        listenToRecitation = ListenToRecitationUseCase(speech),
        measureRecitation = MeasureRecitationUseCase(),
    )

    @Test
    fun `parcourt une séance jusqu'au bilan`() = runTest(dispatcher) {
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1")))
        val vm = viewModel(learning)
        vm.state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()
            val first = expectMostRecentItem()
            assertEquals(2, first.totalSteps)
            val choose = assertIs<ExerciseUi.ChooseReference>(first.exercise)
            assertFalse(first.canCheck)

            vm.onEvent(SessionEvent.OptionSelected("Référence v1"))
            dispatcher.scheduler.advanceUntilIdle()
            assertTrue(expectMostRecentItem().canCheck)

            vm.onEvent(SessionEvent.CheckClicked)
            dispatcher.scheduler.advanceUntilIdle()
            val feedback = assertNotNull(expectMostRecentItem().feedback)
            assertTrue(feedback.isCorrect)
            assertEquals(choose.reference, feedback.reference)

            vm.onEvent(SessionEvent.ContinueClicked)
            dispatcher.scheduler.advanceUntilIdle()
            val second = expectMostRecentItem()
            assertNull(second.feedback)
            val reorder = assertIs<ExerciseUi.Reorder>(second.exercise)

            // Mauvais ordre volontaire : les blocs pris à l'envers.
            reorder.available.sortedByDescending { it.index }.forEach { vm.onEvent(SessionEvent.ChunkPlaced(it.index)) }
            vm.onEvent(SessionEvent.CheckClicked)
            dispatcher.scheduler.advanceUntilIdle()
            assertFalse(assertNotNull(expectMostRecentItem().feedback).isCorrect)

            vm.onEvent(SessionEvent.ContinueClicked)
            dispatcher.scheduler.advanceUntilIdle()
            val summary = assertNotNull(expectMostRecentItem().summary)
            assertEquals(10, summary.xp)
            assertEquals(50, summary.accuracyPercent)
        }
    }

    @Test
    fun `la scène d'arrosage précède le bilan et se passe d'un tap`() = runTest(dispatcher) {
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1")))
        val vm = viewModel(learning)
        vm.state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()
            vm.onEvent(SessionEvent.OptionSelected("Référence v1"))
            vm.onEvent(SessionEvent.CheckClicked)
            dispatcher.scheduler.advanceUntilIdle()
            vm.onEvent(SessionEvent.ContinueClicked)
            dispatcher.scheduler.advanceUntilIdle()
            val reorder = assertIs<ExerciseUi.Reorder>(expectMostRecentItem().exercise)
            reorder.available.sortedByDescending { it.index }.forEach { vm.onEvent(SessionEvent.ChunkPlaced(it.index)) }
            vm.onEvent(SessionEvent.CheckClicked)
            dispatcher.scheduler.advanceUntilIdle()
            vm.onEvent(SessionEvent.ContinueClicked)
            dispatcher.scheduler.advanceUntilIdle()

            // Une réussite sans changement de stade : la graine est arrosée et « prend force ».
            val ending = expectMostRecentItem()
            assertEquals(CeremonyUi.Watering("Référence v1", StageUi.SEED, StageUi.SEED), ending.ceremony)
            assertNotNull(ending.summary)

            vm.onEvent(SessionEvent.CeremonyFinished)
            dispatcher.scheduler.advanceUntilIdle()

            val summary = expectMostRecentItem()
            assertNull(summary.ceremony)
            assertEquals(10, assertNotNull(summary.summary).xp)
        }
    }

    @Test
    fun `affiche la croissance quand le verset change de stade`() = runTest(dispatcher) {
        // successCount = 2 : premier exercice de niveau 3 (mots manquants), la 3e réussite fait passer Racine.
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1").copy(successCount = 2)))
        val vm = viewModel(learning)
        vm.state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()
            val fill = assertIs<ExerciseUi.FillBlanks>(expectMostRecentItem().exercise)
            val words = bank.verses.first { it.id == "v1" }.text.split(' ')
            fill.segments.forEachIndexed { i, segment ->
                if (segment is SegmentUi.Blank) vm.onEvent(SessionEvent.WordChoiceTapped(fill.choices.first { it.text == words[i] }.index))
            }
            vm.onEvent(SessionEvent.CheckClicked)
            dispatcher.scheduler.advanceUntilIdle()

            val feedback = assertNotNull(expectMostRecentItem().feedback)
            assertTrue(feedback.isCorrect)
            assertEquals(StageUi.ROOT, feedback.grewTo)
            assertEquals(MasteryStage.ROOT, learning.verses.value.single().stage)
        }
    }

    @Test
    fun `vider un trou rend le mot à nouveau disponible`() = runTest(dispatcher) {
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1").copy(successCount = 2)))
        val vm = viewModel(learning)
        vm.state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()
            val fill = assertIs<ExerciseUi.FillBlanks>(expectMostRecentItem().exercise)
            vm.onEvent(SessionEvent.WordChoiceTapped(fill.choices.first().index))
            dispatcher.scheduler.advanceUntilIdle()
            assertTrue(assertIs<ExerciseUi.FillBlanks>(expectMostRecentItem().exercise).choices.first().isUsed)

            vm.onEvent(SessionEvent.BlankTapped(0))
            dispatcher.scheduler.advanceUntilIdle()
            assertFalse(assertIs<ExerciseUi.FillBlanks>(expectMostRecentItem().exercise).choices.first().isUsed)
        }
    }

    @Test
    fun `les trous écrits se valident une fois tous remplis`() = runTest(dispatcher) {
        // Racine sans réussite : premier exercice de niveau 4 (trous à écrire).
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1", MasteryStage.ROOT)))
        val vm = viewModel(learning)
        vm.state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()
            val exercise = assertIs<ExerciseUi.TypeBlanks>(expectMostRecentItem().exercise)
            val words = bank.verses.first { it.id == "v1" }.text.split(' ')
            val blanks = exercise.segments.withIndex().filter { it.value is TypedSegmentUi.Blank }

            blanks.forEach { (i, segment) -> vm.onEvent(SessionEvent.BlankTextChanged((segment as TypedSegmentUi.Blank).slot, words[i])) }
            dispatcher.scheduler.advanceUntilIdle()
            assertTrue(expectMostRecentItem().canCheck)

            vm.onEvent(SessionEvent.CheckClicked)
            dispatcher.scheduler.advanceUntilIdle()
            assertTrue(assertNotNull(expectMostRecentItem().feedback).isCorrect)
        }
    }

    @Test
    fun `la récitation orale remplit la transcription et se valide`() = runTest(dispatcher) {
        // Arbre avec 2 réussites : premier exercice de niveau 8 (oral).
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1", MasteryStage.TREE).copy(successCount = 2)))
        val text = bank.verses.first { it.id == "v1" }.text
        speech.events = listOf(SpeechEvent.Listening, SpeechEvent.Partial("mot1 alpha"), SpeechEvent.Final(text))
        val vm = viewModel(learning)
        vm.state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()
            assertIs<ExerciseUi.ReciteAloud>(expectMostRecentItem().exercise)

            vm.onEvent(SessionEvent.MicClicked)
            dispatcher.scheduler.advanceUntilIdle()
            val heard = assertIs<ExerciseUi.ReciteAloud>(expectMostRecentItem().exercise)
            assertEquals(text, heard.transcript)
            assertFalse(heard.isListening)

            vm.onEvent(SessionEvent.CheckClicked)
            dispatcher.scheduler.advanceUntilIdle()
            assertTrue(assertNotNull(expectMostRecentItem().feedback).isCorrect)
        }
    }

    @Test
    fun `un refus du micro affiche l'erreur et permet d'écrire à la place`() = runTest(dispatcher) {
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1", MasteryStage.TREE).copy(successCount = 2)))
        val vm = viewModel(learning)
        vm.state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()
            vm.onEvent(SessionEvent.MicPermissionDenied)
            dispatcher.scheduler.advanceUntilIdle()
            assertEquals(SpeechErrorUi.PERMISSION_DENIED, assertIs<ExerciseUi.ReciteAloud>(expectMostRecentItem().exercise).error)

            vm.onEvent(SessionEvent.TypeInsteadClicked)
            dispatcher.scheduler.advanceUntilIdle()
            val typing = assertIs<ExerciseUi.ReciteAloud>(expectMostRecentItem().exercise)
            assertTrue(typing.isTyping)
            assertNull(typing.error)
        }
    }

    @Test
    fun `une écoute qui n'entend rien le signale`() = runTest(dispatcher) {
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1", MasteryStage.TREE).copy(successCount = 2)))
        speech.events = listOf(SpeechEvent.Listening, SpeechEvent.Failed(SpeechError.NO_MATCH))
        val vm = viewModel(learning)
        vm.state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()
            vm.onEvent(SessionEvent.MicClicked)
            dispatcher.scheduler.advanceUntilIdle()

            val exercise = assertIs<ExerciseUi.ReciteAloud>(expectMostRecentItem().exercise)
            assertEquals(SpeechErrorUi.NO_MATCH, exercise.error)
            assertFalse(exercise.isListening)
        }
    }

    @Test
    fun `quitter avant toute réponse ferme directement`() = runTest(dispatcher) {
        val vm = viewModel(FakeLearningRepository(initial = listOf(learningVerse("v1"))))
        vm.state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()
            vm.onEvent(SessionEvent.CloseClicked)
            dispatcher.scheduler.advanceUntilIdle()

            val state = expectMostRecentItem()
            assertTrue(state.shouldClose)
            assertFalse(state.isConfirmingQuit)
        }
    }

    @Test
    fun `quitter après une réponse demande confirmation`() = runTest(dispatcher) {
        // Régression QA : quitter en pleine séance ne demandait rien.
        val vm = viewModel(FakeLearningRepository(initial = listOf(learningVerse("v1"))))
        vm.state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()
            vm.onEvent(SessionEvent.OptionSelected("Référence v1"))
            vm.onEvent(SessionEvent.CheckClicked)
            dispatcher.scheduler.advanceUntilIdle()

            vm.onEvent(SessionEvent.CloseClicked)
            dispatcher.scheduler.advanceUntilIdle()
            assertTrue(expectMostRecentItem().isConfirmingQuit)

            vm.onEvent(SessionEvent.QuitCancelled)
            dispatcher.scheduler.advanceUntilIdle()
            val cancelled = expectMostRecentItem()
            assertFalse(cancelled.isConfirmingQuit)
            assertFalse(cancelled.shouldClose)

            vm.onEvent(SessionEvent.CloseClicked)
            vm.onEvent(SessionEvent.QuitConfirmed)
            dispatcher.scheduler.advanceUntilIdle()
            assertTrue(expectMostRecentItem().shouldClose)
        }
    }

    @Test
    fun `un pack de langue absent est expliqué tel quel`() = runTest(dispatcher) {
        // Régression QA : « L'écoute s'est interrompue » au lieu d'expliquer le pack manquant.
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1", MasteryStage.TREE).copy(successCount = 2)))
        speech.events = listOf(SpeechEvent.Failed(SpeechError.LANGUAGE_PACK_MISSING))
        val vm = viewModel(learning)
        vm.state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()
            vm.onEvent(SessionEvent.MicClicked)
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(SpeechErrorUi.LANGUAGE_PACK_MISSING, assertIs<ExerciseUi.ReciteAloud>(expectMostRecentItem().exercise).error)
        }
    }

    @Test
    fun `une pause ne fait pas perdre les mots déjà dits`() = runTest(dispatcher) {
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1", MasteryStage.TREE).copy(successCount = 2)))
        val vm = viewModel(learning)
        vm.state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()
            speech.events = listOf(SpeechEvent.Partial("mot1 alpha"), SpeechEvent.Final("mot1 alpha beta gamma"))
            vm.onEvent(SessionEvent.MicClicked)
            dispatcher.scheduler.advanceUntilIdle()
            speech.events = listOf(SpeechEvent.Final("delta epsilon zeta eta theta"))
            vm.onEvent(SessionEvent.MicClicked)
            dispatcher.scheduler.advanceUntilIdle()

            val heard = assertIs<ExerciseUi.ReciteAloud>(expectMostRecentItem().exercise)
            assertEquals("mot1 alpha beta gamma delta epsilon zeta eta theta", heard.transcript)
            assertTrue(heard.words.all { it.state == RecitedWordState.RECITED })
            assertEquals(1f, heard.fill)
        }
    }

    @Test
    fun `l'écoute s'arrête d'elle-même quand la goutte est pleine`() = runTest(dispatcher) {
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1", MasteryStage.TREE).copy(successCount = 2)))
        speech.events = listOf(SpeechEvent.Partial("mot1 alpha beta"), SpeechEvent.Partial("mot1 alpha beta gamma delta epsilon zeta eta theta"))
        val vm = viewModel(learning)
        vm.state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()
            vm.onEvent(SessionEvent.MicClicked)
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(1, speech.stopCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `l'écoute continue tant que la goutte n'est pas pleine`() = runTest(dispatcher) {
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1", MasteryStage.TREE).copy(successCount = 2)))
        speech.events = listOf(SpeechEvent.Partial("mot1 alpha beta gamma"))
        val vm = viewModel(learning)
        vm.state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()
            vm.onEvent(SessionEvent.MicClicked)
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(0, speech.stopCount)
            val heard = assertIs<ExerciseUi.ReciteAloud>(expectMostRecentItem().exercise)
            assertEquals(4, heard.words.count { it.state == RecitedWordState.RECITED })
            assertTrue(heard.fill in 0.5f..0.6f)
        }
    }

    @Test
    fun `apres ecrire plutot - on peut revenir a l'oral avec une goutte vide`() = runTest(dispatcher) {
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1", MasteryStage.TREE).copy(successCount = 2)))
        val vm = viewModel(learning)
        vm.state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()
            vm.onEvent(SessionEvent.TypeInsteadClicked)
            vm.onEvent(SessionEvent.TextChanged("mot1 alpha beta"))
            vm.onEvent(SessionEvent.SpeakInsteadClicked)
            dispatcher.scheduler.advanceUntilIdle()

            val oral = assertIs<ExerciseUi.ReciteAloud>(expectMostRecentItem().exercise)
            assertFalse(oral.isTyping)
            assertEquals(0f, oral.fill)
            assertTrue(oral.words.none { it.state == RecitedWordState.RECITED })
        }
    }

    @Test
    fun `ecrire plutot pendant l'ecoute arrete l'indicateur d'ecoute`() = runTest(dispatcher) {
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1", MasteryStage.TREE).copy(successCount = 2)))
        speech.events = listOf(SpeechEvent.Listening, SpeechEvent.Partial("mot1 alpha"))
        speech.keepListening = true
        val vm = viewModel(learning)
        vm.state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()
            vm.onEvent(SessionEvent.MicClicked)
            dispatcher.scheduler.advanceUntilIdle()
            assertTrue(assertIs<ExerciseUi.ReciteAloud>(expectMostRecentItem().exercise).isListening)

            vm.onEvent(SessionEvent.TypeInsteadClicked)
            dispatcher.scheduler.advanceUntilIdle()
            val typing = assertIs<ExerciseUi.ReciteAloud>(expectMostRecentItem().exercise)
            assertTrue(typing.isTyping)
            assertFalse(typing.isListening)
        }
    }

    @Test
    fun `verifier pendant l'ecoute arrete l'indicateur d'ecoute`() = runTest(dispatcher) {
        val learning = FakeLearningRepository(initial = listOf(learningVerse("v1", MasteryStage.TREE).copy(successCount = 2)))
        speech.events = listOf(SpeechEvent.Partial("mot1 alpha"))
        speech.keepListening = true
        val vm = viewModel(learning)
        vm.state.test {
            skipItems(1)
            dispatcher.scheduler.advanceUntilIdle()
            vm.onEvent(SessionEvent.MicClicked)
            dispatcher.scheduler.advanceUntilIdle()
            assertTrue(assertIs<ExerciseUi.ReciteAloud>(expectMostRecentItem().exercise).isListening)

            vm.onEvent(SessionEvent.CheckClicked)
            dispatcher.scheduler.advanceUntilIdle()
            val state = expectMostRecentItem()
            assertNotNull(state.feedback)
            assertFalse(assertIs<ExerciseUi.ReciteAloud>(state.exercise).isListening)
        }
    }
}
