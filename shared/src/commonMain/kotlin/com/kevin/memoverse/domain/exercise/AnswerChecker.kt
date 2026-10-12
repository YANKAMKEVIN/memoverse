package com.kevin.memoverse.domain.exercise

import com.kevin.memoverse.domain.model.Exercise
import com.kevin.memoverse.domain.model.ExerciseAnswer
import com.kevin.memoverse.domain.model.RecitationProgress

/**
 * Correction tolérante du cadrage : on ignore la casse, les accents et la ponctuation,
 * et on accepte une faute de frappe par mot (mots de plus de 3 lettres).
 */
object AnswerChecker {

    fun isCorrect(exercise: Exercise, answer: ExerciseAnswer): Boolean = when (exercise) {
        is Exercise.ChooseReference -> answer is ExerciseAnswer.Choice && answer.option == exercise.verse.reference
        is Exercise.CompletePart -> answer is ExerciseAnswer.Choice && answer.option == exercise.parts.hidden
        is Exercise.Reorder -> answer is ExerciseAnswer.Order &&
            answer.chunkIndexes.map { exercise.chunks[it] } == exercise.chunks
        is Exercise.FillBlanks -> answer is ExerciseAnswer.Blanks &&
            answer.words.map { normalize(it) } == exercise.blankIndexes.map { normalize(exercise.words[it]) }
        is Exercise.TypePart -> answer is ExerciseAnswer.Typed && isTypedTextCorrect(exercise.parts.hidden, answer.text)
        is Exercise.TypeBlanks -> answer is ExerciseAnswer.Blanks &&
            answer.words.size == exercise.blankIndexes.size &&
            exercise.blankIndexes.zip(answer.words).all { (index, typed) -> isTypedTextCorrect(exercise.words[index], typed) }
        is Exercise.Recite -> answer is ExerciseAnswer.Typed && isTypedTextCorrect(exercise.verse.text, answer.text)
        is Exercise.ReciteAloud -> when (answer) {
            is ExerciseAnswer.Spoken -> isRecitationCorrect(exercise.verse.text, answer.text)
            // « Écrire plutôt » : à l'écrit, tous les mots sont exigés.
            is ExerciseAnswer.Typed -> isTypedTextCorrect(exercise.verse.text, answer.text)
            else -> false
        }
    }

    /** Récitation orale : réussie si au moins 85 % des mots du verset sont retrouvés dans l'ordre. */
    fun isRecitationCorrect(expected: String, spoken: String): Boolean = recitationProgress(expected, spoken).isComplete

    /**
     * Aligne la transcription sur le verset (plus longue sous-suite commune, une faute tolérée par mot long)
     * pour savoir quels mots ont déjà été dits.
     */
    fun recitationProgress(expected: String, spoken: String): RecitationProgress {
        val tokens = expected.split(WHITESPACE).filter { it.isNotEmpty() }
        // Chaque mot normalisé garde le rang du mot affiché d'où il vient (« Lui-même » en donne deux).
        val expectedWords = tokens.flatMapIndexed { index, token -> words(token).map { it to index } }
        val spokenWords = words(spoken)
        if (expectedWords.isEmpty()) return RecitationProgress(tokens.map { false }, 0.0)
        val lcs = Array(expectedWords.size + 1) { IntArray(spokenWords.size + 1) }
        for (i in 1..expectedWords.size) {
            for (j in 1..spokenWords.size) {
                lcs[i][j] = if (isSameWord(expectedWords[i - 1].first, spokenWords[j - 1])) lcs[i - 1][j - 1] + 1
                else maxOf(lcs[i - 1][j], lcs[i][j - 1])
            }
        }
        val matched = BooleanArray(expectedWords.size)
        var i = expectedWords.size
        var j = spokenWords.size
        while (i > 0 && j > 0) {
            when {
                isSameWord(expectedWords[i - 1].first, spokenWords[j - 1]) && lcs[i][j] == lcs[i - 1][j - 1] + 1 -> {
                    matched[i - 1] = true; i--; j--
                }
                lcs[i - 1][j] >= lcs[i][j - 1] -> i--
                else -> j--
            }
        }
        val recited = tokens.indices.map { index ->
            val parts = expectedWords.indices.filter { expectedWords[it].second == index }
            // Un signe seul (« — ») n'a rien à dire : il suit le mot qui le précède.
            if (parts.isEmpty()) false else parts.any { matched[it] }
        }.toMutableList()
        for (index in tokens.indices) {
            if (words(tokens[index]).isEmpty()) recited[index] = index > 0 && recited[index - 1]
        }
        return RecitationProgress(recited, matched.count { it }.toDouble() / expectedWords.size)
    }

    private fun isSameWord(expected: String, actual: String) = levenshtein(expected, actual) <= if (expected.length > 3) 1 else 0

    private val WHITESPACE = Regex("\\s+")

    fun isTypedTextCorrect(expected: String, typed: String): Boolean {
        val expectedWords = words(expected)
        val typedWords = words(typed)
        if (expectedWords.size != typedWords.size) return false
        return expectedWords.zip(typedWords).all { (e, t) -> isSameWord(e, t) }
    }

    fun normalize(text: String): String = words(text).joinToString(" ")

    private fun words(text: String): List<String> {
        val builder = StringBuilder()
        for (c in text.lowercase()) {
            when {
                c in APOSTROPHES -> Unit
                c.isLetterOrDigit() -> builder.append(ACCENTS[c] ?: c.toString())
                else -> builder.append(' ')
            }
        }
        return builder.split(' ').filter { it.isNotEmpty() }
    }

    private fun levenshtein(a: String, b: String): Int {
        var previous = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            val current = IntArray(b.length + 1)
            current[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                current[j] = minOf(previous[j] + 1, current[j - 1] + 1, previous[j - 1] + cost)
            }
            previous = current
        }
        return previous[b.length]
    }

    private val APOSTROPHES = setOf('\'', '’', 'ʼ')
    private val ACCENTS: Map<Char, String> = buildMap {
        "àâä".forEach { put(it, "a") }
        "éèêë".forEach { put(it, "e") }
        "îï".forEach { put(it, "i") }
        "ôö".forEach { put(it, "o") }
        "ùûü".forEach { put(it, "u") }
        put('ç', "c"); put('ÿ', "y"); put('œ', "oe"); put('æ', "ae")
    }
}
