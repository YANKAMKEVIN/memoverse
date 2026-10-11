package com.kevin.memoverse.domain.exercise

import com.kevin.memoverse.domain.model.Exercise
import com.kevin.memoverse.domain.model.HiddenPosition
import com.kevin.memoverse.fake.verse
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ExerciseFactoryTest {

    private val target = verse("hebreux-11-1", text = "Or la foi est une ferme assurance des choses qu’on espère, une démonstration de celles qu’on ne voit pas.")
    private val pool = listOf(
        target,
        verse("romains-10-17", text = "Ainsi la foi vient de ce qu’on entend, et ce qu’on entend vient de la parole de Christ."),
        verse("jean-3-16", text = "Car Dieu a tant aimé le monde qu’il a donné son Fils unique, afin que quiconque croit en lui ne périsse point."),
        verse("psaumes-23-1", text = "L’Éternel est mon berger : je ne manquerai de rien."),
    )
    private val factory = ExerciseFactory(Random(42))

    @Test
    fun `la référence fait partie des 4 propositions`() {
        val exercise = assertIs<Exercise.ChooseReference>(factory.create(1, target, pool))

        assertEquals(4, exercise.options.size)
        assertTrue(target.reference in exercise.options)
    }

    @Test
    fun `les blocs mélangés recomposent exactement le verset`() {
        val exercise = assertIs<Exercise.Reorder>(factory.create(2, target, pool))

        assertEquals(target.text, exercise.chunks.joinToString(" "))
        assertEquals(exercise.chunks.indices.toSet(), exercise.shuffledOrder.toSet())
        assertNotEquals(exercise.chunks, exercise.shuffledOrder.map { exercise.chunks[it] })
    }

    @Test
    fun `les mots retirés figurent parmi les propositions`() {
        val exercise = assertIs<Exercise.FillBlanks>(factory.create(3, target, pool))

        assertTrue(exercise.blankIndexes.size >= 2)
        exercise.blankIndexes.forEach { assertTrue(exercise.words[it] in exercise.choices) }
    }

    @Test
    fun `la partie cachée est l'une des 3 propositions et recompose le verset`() {
        val exercise = assertIs<Exercise.CompletePart>(factory.create(5, target, pool))

        assertEquals(3, exercise.options.size)
        assertTrue(exercise.parts.hidden in exercise.options)
        val rebuilt = listOf(exercise.parts.before, exercise.parts.hidden, exercise.parts.after).filter { it.isNotEmpty() }.joinToString(" ")
        assertEquals(target.text, rebuilt)
    }

    @Test
    fun `coupe le verset en tiers selon la position`() {
        val parts = ExerciseFactory.split("un deux trois quatre cinq six", HiddenPosition.END)

        assertEquals("un deux trois quatre", parts.before)
        assertEquals("cinq six", parts.hidden)
        assertEquals("", parts.after)
    }

    @Test
    fun `le niveau 4 fait écrire des mots retirés, sans propositions`() {
        val exercise = assertIs<Exercise.TypeBlanks>(factory.create(4, target, pool))

        assertTrue(exercise.blankIndexes.isNotEmpty())
        assertEquals(target.text, exercise.words.joinToString(" "))
    }

    @Test
    fun `plus de réussites dans le stade donnent plus de trous`() {
        val easy = assertIs<Exercise.TypeBlanks>(ExerciseFactory(Random(1)).create(4, target, pool, progress = 0))
        val hard = assertIs<Exercise.TypeBlanks>(ExerciseFactory(Random(1)).create(4, target, pool, progress = 3))

        assertTrue(hard.blankIndexes.size > easy.blankIndexes.size)
    }

    @Test
    fun `plus de réussites donnent plus de blocs à remettre en ordre`() {
        val easy = assertIs<Exercise.Reorder>(ExerciseFactory(Random(1)).create(2, target, pool, progress = 0))
        val hard = assertIs<Exercise.Reorder>(ExerciseFactory(Random(1)).create(2, target, pool, progress = 2))

        assertTrue(hard.chunks.size > easy.chunks.size)
    }

    @Test
    fun `le niveau 7 fait réciter par écrit et le niveau 8 à voix haute`() {
        assertIs<Exercise.Recite>(factory.create(7, target, pool))
        assertIs<Exercise.ReciteAloud>(factory.create(8, target, pool))
    }

    @Test
    fun `sans reconnaissance vocale, l'oral devient une récitation écrite`() {
        assertIs<Exercise.Recite>(ExerciseFactory(Random(1), isSpeechAvailable = false).create(8, target, pool))
    }

    @Test
    fun `la ponctuation isolée reste collée au mot précédent`() {
        // Régression QA : un galet commençait par « ; ».
        assertEquals(
            listOf("agréable ;", "car", "il", "faut :", "Dieu", "existe !"),
            ExerciseFactory.words("agréable ; car il faut : Dieu existe !"),
        )
    }

    @Test
    fun `aucun galet ne dépasse 6 mots, même pour un verset long`() {
        // Régression QA : un galet de Matthieu 28:19-20 tenait sur deux lignes.
        val long = verse("matthieu-28-19-20", text = (1..45).joinToString(" ") { "mot$it" })

        val exercise = assertIs<Exercise.Reorder>(factory.create(2, long, pool + long))

        assertTrue(exercise.chunks.all { ExerciseFactory.words(it).size <= 6 })
        assertEquals(long.text, exercise.chunks.joinToString(" "))
    }
}
