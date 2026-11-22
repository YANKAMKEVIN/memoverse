package com.kevin.memoverse.presentation.session

import com.kevin.memoverse.domain.model.Difficulty
import com.kevin.memoverse.domain.model.Exercise
import com.kevin.memoverse.domain.model.HiddenPosition
import com.kevin.memoverse.domain.model.LearningVerse
import com.kevin.memoverse.domain.model.MasteryStage
import com.kevin.memoverse.domain.model.Verse
import com.kevin.memoverse.domain.model.VerseParts
import com.kevin.memoverse.domain.model.WateringStats
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Sauvegarde d'une séance en cours, pour la retrouver quand Android recrée l'app (processus tué en arrière-plan).
 *
 * Toute la séance est gardée : les exercices tirés au hasard (sinon ils changeraient), la position, la réponse en
 * cours, le verdict affiché et le bilan partiel. Le micro, lui, repart éteint. Le format est privé à l'app : une
 * sauvegarde illisible (autre version, données abîmées) est ignorée et la séance repart de zéro.
 */
internal object SessionSnapshotCodec {

    private val json = Json { ignoreUnknownKeys = true }

    /** Texte à sauvegarder, ou null si l'écran n'a rien à retrouver (chargement, erreur, séance vide ou quittée). */
    fun encode(model: SessionModel): String? {
        if (model.isLoading || model.hasError || model.shouldClose || model.exercises.isEmpty()) return null
        return json.encodeToString(SessionSnapshot.serializer(), model.toSnapshot())
    }

    fun decode(text: String?): SessionModel? {
        if (text.isNullOrEmpty()) return null
        val snapshot = runCatching { json.decodeFromString(SessionSnapshot.serializer(), text) }.getOrNull() ?: return null
        val model = runCatching { snapshot.toModel() }.getOrNull() ?: return null
        val isConsistent = model.exercises.isNotEmpty() && (model.isFinished || model.index in model.exercises.indices)
        return model.takeIf { isConsistent }
    }
}

@Serializable
internal data class SessionSnapshot(
    val exercises: List<ExerciseSnapshot>,
    val index: Int,
    val draft: DraftSnapshot? = null,
    val lastResult: ResultSnapshot? = null,
    val verses: List<LearningVerseSnapshot>,
    val initialStages: Map<String, MasteryStage>,
    val correctCount: Int,
    val isFinished: Boolean,
    val wateredVerseIds: List<String>,
    val isCeremonyDone: Boolean,
    val isConfirmingQuit: Boolean,
    val stats: StatsSnapshot? = null,
    val isTyping: Boolean,
    val committedText: String,
    val typedText: String,
)

@Serializable
internal data class VerseSnapshot(
    val id: String,
    val reference: String,
    val text: String,
    val context: String,
    val wordCount: Int,
    val difficulty: Difficulty,
    val themeIds: List<String>,
)

@Serializable
internal data class PartsSnapshot(val before: String, val hidden: String, val after: String, val position: HiddenPosition)

@Serializable
internal sealed interface ExerciseSnapshot {
    val verse: VerseSnapshot

    @Serializable @SerialName("chooseReference")
    data class ChooseReference(override val verse: VerseSnapshot, val options: List<String>) : ExerciseSnapshot

    @Serializable @SerialName("reorder")
    data class Reorder(override val verse: VerseSnapshot, val chunks: List<String>, val shuffledOrder: List<Int>) : ExerciseSnapshot

    @Serializable @SerialName("fillBlanks")
    data class FillBlanks(override val verse: VerseSnapshot, val words: List<String>, val blankIndexes: List<Int>, val choices: List<String>) : ExerciseSnapshot

    @Serializable @SerialName("typeBlanks")
    data class TypeBlanks(override val verse: VerseSnapshot, val words: List<String>, val blankIndexes: List<Int>) : ExerciseSnapshot

    @Serializable @SerialName("completePart")
    data class CompletePart(override val verse: VerseSnapshot, val parts: PartsSnapshot, val options: List<String>) : ExerciseSnapshot

    @Serializable @SerialName("typePart")
    data class TypePart(override val verse: VerseSnapshot, val parts: PartsSnapshot) : ExerciseSnapshot

    @Serializable @SerialName("recite")
    data class Recite(override val verse: VerseSnapshot) : ExerciseSnapshot

    @Serializable @SerialName("reciteAloud")
    data class ReciteAloud(override val verse: VerseSnapshot) : ExerciseSnapshot
}

@Serializable
internal sealed interface DraftSnapshot {
    @Serializable @SerialName("choice")
    data class Choice(val selected: String? = null) : DraftSnapshot

    @Serializable @SerialName("order")
    data class Order(val placed: List<Int>) : DraftSnapshot

    @Serializable @SerialName("blanks")
    data class Blanks(val filled: List<Int?>) : DraftSnapshot

    @Serializable @SerialName("typed")
    data class Typed(val text: String, val fromSpeech: Boolean) : DraftSnapshot

    @Serializable @SerialName("typedBlanks")
    data class TypedBlanks(val values: List<String>) : DraftSnapshot
}

@Serializable
internal data class ResultSnapshot(
    val isCorrect: Boolean,
    val grewTo: MasteryStage? = null,
    val wiltedTo: MasteryStage? = null,
    val notSaved: Boolean = false,
)

@Serializable
internal data class LearningVerseSnapshot(
    val verseId: String,
    val themeId: String,
    val stage: MasteryStage,
    val addedAtEpochMillis: Long,
    val successCount: Int,
    val successDays: Int,
    val lastSuccessEpochDay: Long? = null,
    val consecutiveFailures: Int,
    val reviewStep: Int,
    val dueAtEpochMillis: Long,
)

@Serializable
internal data class StatsSnapshot(val totalXp: Int, val streakDays: Int, val lastSessionEpochDay: Long? = null)

private fun SessionModel.toSnapshot() = SessionSnapshot(
    exercises = exercises.map { it.toSnapshot() },
    index = index,
    draft = draft?.toSnapshot(),
    lastResult = lastResult?.let { ResultSnapshot(it.isCorrect, it.grewTo, it.wiltedTo, it.notSaved) },
    verses = verses.values.map { it.toSnapshot() },
    initialStages = initialStages,
    correctCount = correctCount,
    isFinished = isFinished,
    wateredVerseIds = wateredVerseIds.toList(),
    isCeremonyDone = isCeremonyDone,
    isConfirmingQuit = isConfirmingQuit,
    stats = stats?.let { StatsSnapshot(it.totalXp, it.streakDays, it.lastSessionEpochDay) },
    isTyping = speech.isTyping,
    committedText = speech.committedText,
    typedText = speech.typedText,
)

private fun SessionSnapshot.toModel() = SessionModel(
    isLoading = false,
    exercises = exercises.map { it.toExercise() },
    index = index,
    draft = draft?.toDraft(),
    lastResult = lastResult?.let { ExerciseResult(it.isCorrect, it.grewTo, it.wiltedTo, it.notSaved) },
    verses = verses.associate { it.verseId to it.toLearningVerse() },
    initialStages = initialStages,
    correctCount = correctCount,
    isFinished = isFinished,
    wateredVerseIds = wateredVerseIds.toSet(),
    isCeremonyDone = isCeremonyDone,
    // Le micro ne se rallume pas tout seul : on retrouve ce qui avait été dit, sans écoute en cours.
    speech = SpeechModel(isTyping = isTyping, committedText = committedText, typedText = typedText),
    isConfirmingQuit = isConfirmingQuit,
    stats = stats?.let { WateringStats(it.totalXp, it.streakDays, it.lastSessionEpochDay) },
)

private fun Verse.toSnapshot() = VerseSnapshot(id, reference, text, context, wordCount, difficulty, themeIds)
private fun VerseSnapshot.toVerse() = Verse(id, reference, text, context, wordCount, difficulty, themeIds)
private fun VerseParts.toSnapshot() = PartsSnapshot(before, hidden, after, position)
private fun PartsSnapshot.toParts() = VerseParts(before, hidden, after, position)

private fun Exercise.toSnapshot(): ExerciseSnapshot = when (this) {
    is Exercise.ChooseReference -> ExerciseSnapshot.ChooseReference(verse.toSnapshot(), options)
    is Exercise.Reorder -> ExerciseSnapshot.Reorder(verse.toSnapshot(), chunks, shuffledOrder)
    is Exercise.FillBlanks -> ExerciseSnapshot.FillBlanks(verse.toSnapshot(), words, blankIndexes, choices)
    is Exercise.TypeBlanks -> ExerciseSnapshot.TypeBlanks(verse.toSnapshot(), words, blankIndexes)
    is Exercise.CompletePart -> ExerciseSnapshot.CompletePart(verse.toSnapshot(), parts.toSnapshot(), options)
    is Exercise.TypePart -> ExerciseSnapshot.TypePart(verse.toSnapshot(), parts.toSnapshot())
    is Exercise.Recite -> ExerciseSnapshot.Recite(verse.toSnapshot())
    is Exercise.ReciteAloud -> ExerciseSnapshot.ReciteAloud(verse.toSnapshot())
}

private fun ExerciseSnapshot.toExercise(): Exercise = when (this) {
    is ExerciseSnapshot.ChooseReference -> Exercise.ChooseReference(verse.toVerse(), options)
    is ExerciseSnapshot.Reorder -> Exercise.Reorder(verse.toVerse(), chunks, shuffledOrder)
    is ExerciseSnapshot.FillBlanks -> Exercise.FillBlanks(verse.toVerse(), words, blankIndexes, choices)
    is ExerciseSnapshot.TypeBlanks -> Exercise.TypeBlanks(verse.toVerse(), words, blankIndexes)
    is ExerciseSnapshot.CompletePart -> Exercise.CompletePart(verse.toVerse(), parts.toParts(), options)
    is ExerciseSnapshot.TypePart -> Exercise.TypePart(verse.toVerse(), parts.toParts())
    is ExerciseSnapshot.Recite -> Exercise.Recite(verse.toVerse())
    is ExerciseSnapshot.ReciteAloud -> Exercise.ReciteAloud(verse.toVerse())
}

private fun AnswerDraft.toSnapshot(): DraftSnapshot = when (this) {
    is AnswerDraft.Choice -> DraftSnapshot.Choice(selected)
    is AnswerDraft.Order -> DraftSnapshot.Order(placed)
    is AnswerDraft.Blanks -> DraftSnapshot.Blanks(filled)
    is AnswerDraft.Typed -> DraftSnapshot.Typed(text, fromSpeech)
    is AnswerDraft.TypedBlanks -> DraftSnapshot.TypedBlanks(values)
}

private fun DraftSnapshot.toDraft(): AnswerDraft = when (this) {
    is DraftSnapshot.Choice -> AnswerDraft.Choice(selected)
    is DraftSnapshot.Order -> AnswerDraft.Order(placed)
    is DraftSnapshot.Blanks -> AnswerDraft.Blanks(filled)
    is DraftSnapshot.Typed -> AnswerDraft.Typed(text, fromSpeech)
    is DraftSnapshot.TypedBlanks -> AnswerDraft.TypedBlanks(values)
}

private fun LearningVerse.toSnapshot() = LearningVerseSnapshot(
    verseId, themeId, stage, addedAtEpochMillis, successCount, successDays, lastSuccessEpochDay, consecutiveFailures, reviewStep, dueAtEpochMillis,
)

private fun LearningVerseSnapshot.toLearningVerse() = LearningVerse(
    verseId, themeId, stage, addedAtEpochMillis, successCount, successDays, lastSuccessEpochDay, consecutiveFailures, reviewStep, dueAtEpochMillis,
)
