package com.kevin.memoverse.domain.exercise

import com.kevin.memoverse.domain.model.Exercise
import com.kevin.memoverse.domain.model.HiddenPosition
import com.kevin.memoverse.domain.model.Verse
import com.kevin.memoverse.domain.model.VerseParts
import kotlin.random.Random

/**
 * Fabrique les exercices d'un verset ; les distracteurs viennent des autres versets de la banque.
 * [create] reçoit la progression dans le stade (`progress` = réussites du stade) : plus elle monte,
 * plus il y a de trous et de blocs, pour s'assurer que le verset est vraiment maîtrisé.
 */
class ExerciseFactory(
    private val random: Random = Random.Default,
    /** Faux si l'appareil ne sait pas reconnaître la voix : l'oral devient une récitation écrite. */
    private val isSpeechAvailable: Boolean = true,
) {
    fun create(level: Int, verse: Verse, pool: List<Verse>, progress: Int = 0): Exercise {
        val others = pool.filter { it.id != verse.id }
        return when (level) {
            1 -> chooseReference(verse, others)
            2 -> reorder(verse, progress)
            3 -> fillBlanks(verse, others, progress).takeIf { it.blankIndexes.isNotEmpty() } ?: reorder(verse, progress)
            4 -> typeBlanks(verse, progress) ?: Exercise.TypePart(verse, split(verse.text, randomPosition()))
            5 -> completePart(verse, others)
            6 -> Exercise.TypePart(verse, split(verse.text, randomPosition()))
            7 -> Exercise.Recite(verse)
            else -> if (isSpeechAvailable) Exercise.ReciteAloud(verse) else Exercise.Recite(verse)
        }
    }

    private fun chooseReference(verse: Verse, others: List<Verse>): Exercise.ChooseReference {
        val distractors = others.map { it.reference }.filter { it != verse.reference }.distinct().shuffled(random).take(REFERENCE_OPTIONS - 1)
        return Exercise.ChooseReference(verse, (distractors + verse.reference).shuffled(random))
    }

    private fun reorder(verse: Verse, progress: Int = 0): Exercise.Reorder {
        val words = words(verse.text)
        // Assez de blocs pour qu'aucun galet ne dépasse MAX_WORDS_PER_CHUNK mots (une seule ligne).
        val minForLength = (words.size + MAX_WORDS_PER_CHUNK - 1) / MAX_WORDS_PER_CHUNK
        val chunkCount = (words.size / WORDS_PER_CHUNK + progress).coerceIn(MIN_CHUNKS, MAX_CHUNKS + progress)
            .coerceAtLeast(minForLength)
            .coerceAtMost(words.size)
        val chunks = (0 until chunkCount).map { i ->
            words.subList(i * words.size / chunkCount, (i + 1) * words.size / chunkCount).joinToString(" ")
        }
        var order = chunks.indices.shuffled(random)
        // Un mélange identique à l'ordre du verset n'aurait rien à remettre en ordre.
        repeat(SHUFFLE_ATTEMPTS) { if (order.map { chunks[it] } == chunks) order = chunks.indices.shuffled(random) }
        return Exercise.Reorder(verse, chunks, order)
    }

    private fun fillBlanks(verse: Verse, others: List<Verse>, progress: Int = 0): Exercise.FillBlanks {
        val words = words(verse.text)
        val candidates = words.indices.filter { isBlankable(words[it]) }
        val blankCount = (words.size / WORDS_PER_BLANK + progress).coerceIn(MIN_BLANKS, MAX_BLANKS).coerceAtMost(candidates.size)
        val blanks = candidates.shuffled(random).take(blankCount).sorted()
        val answers = blanks.map { words[it] }
        val distractors = others.flatMap { words(it.text) }
            .filter { isBlankable(it) && answers.none { a -> AnswerChecker.normalize(a) == AnswerChecker.normalize(it) } }
            .distinctBy { AnswerChecker.normalize(it) }
            .shuffled(random)
            .take(BLANK_DISTRACTORS)
        return Exercise.FillBlanks(verse, words, blanks, (answers + distractors).shuffled(random))
    }

    private fun typeBlanks(verse: Verse, progress: Int = 0): Exercise.TypeBlanks? {
        val words = words(verse.text)
        val candidates = words.indices.filter { isBlankable(words[it]) }
        if (candidates.isEmpty()) return null
        val blankCount = (words.size / WORDS_PER_TYPED_BLANK + progress).coerceIn(MIN_TYPED_BLANKS, MAX_TYPED_BLANKS).coerceAtMost(candidates.size)
        return Exercise.TypeBlanks(verse, words, candidates.shuffled(random).take(blankCount).sorted())
    }

    private fun completePart(verse: Verse, others: List<Verse>): Exercise.CompletePart {
        val parts = split(verse.text, randomPosition())
        val distractors = others.shuffled(random)
            .map { split(it.text, parts.position).hidden }
            .filter { it.isNotBlank() && it != parts.hidden }
            .distinct()
            .take(PART_OPTIONS - 1)
        return Exercise.CompletePart(verse, parts, (distractors + parts.hidden).shuffled(random))
    }

    private fun randomPosition(): HiddenPosition = HiddenPosition.entries[random.nextInt(HiddenPosition.entries.size)]

    companion object {
        private const val REFERENCE_OPTIONS = 4
        private const val PART_OPTIONS = 3
        private const val WORDS_PER_CHUNK = 3
        private const val MIN_CHUNKS = 3
        private const val MAX_CHUNKS = 6
        private const val MAX_WORDS_PER_CHUNK = 6
        private const val SHUFFLE_ATTEMPTS = 5
        private const val WORDS_PER_BLANK = 8
        private const val MIN_BLANKS = 2
        private const val MAX_BLANKS = 4
        private const val BLANK_DISTRACTORS = 3
        private const val MIN_BLANK_LETTERS = 4
        private const val WORDS_PER_TYPED_BLANK = 10
        private const val MIN_TYPED_BLANKS = 1
        private const val MAX_TYPED_BLANKS = 4

        /**
         * Mots du verset. La ponctuation isolée par la typographie française (« agréable ; car »)
         * reste collée au mot qui la précède, pour qu'aucun galet ne commence par « ; ».
         */
        fun words(text: String): List<String> = text.split(' ').filter { it.isNotBlank() }.fold(mutableListOf()) { acc, token ->
            if (acc.isNotEmpty() && token.none { it.isLetterOrDigit() }) acc[acc.lastIndex] = acc.last() + " " + token else acc += token
            acc
        }

        /** Coupe le verset en trois tiers (en mots) et cache celui de [position]. */
        fun split(text: String, position: HiddenPosition): VerseParts {
            val words = words(text)
            val first = words.size / 3
            val second = words.size * 2 / 3
            val start = words.subList(0, first).joinToString(" ")
            val middle = words.subList(first, second).joinToString(" ")
            val end = words.subList(second, words.size).joinToString(" ")
            return when (position) {
                HiddenPosition.START -> VerseParts("", start, listOf(middle, end).joinToString(" "), position)
                HiddenPosition.MIDDLE -> VerseParts(start, middle, end, position)
                HiddenPosition.END -> VerseParts(listOf(start, middle).joinToString(" "), end, "", position)
            }
        }

        /** Un mot sans ponctuation, assez long pour être intéressant à retrouver. */
        private fun isBlankable(word: String): Boolean =
            word.all { it.isLetter() || it == '’' || it == '\'' || it == '-' } && word.count { it.isLetter() } >= MIN_BLANK_LETTERS
    }
}
