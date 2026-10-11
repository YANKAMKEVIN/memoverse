package com.kevin.memoverse.domain.exercise

import com.kevin.memoverse.domain.model.Exercise
import com.kevin.memoverse.domain.model.ExerciseAnswer
import com.kevin.memoverse.fake.verse
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnswerCheckerTest {

    private val expected = "Or la foi est une ferme assurance des choses qu’on espère,"

    @Test
    fun `ignore la casse, les accents et la ponctuation`() {
        assertTrue(AnswerChecker.isTypedTextCorrect(expected, "or la foi est une ferme assurance des choses quon espere"))
    }

    @Test
    fun `accepte une faute de frappe par mot long`() {
        assertTrue(AnswerChecker.isTypedTextCorrect(expected, "Or la foi est une ferme asurance des choses qu'on espère"))
    }

    @Test
    fun `refuse deux fautes dans le même mot`() {
        assertFalse(AnswerChecker.isTypedTextCorrect(expected, "Or la foi est une ferme asuranse des choses qu'on espère"))
    }

    @Test
    fun `refuse une faute sur un mot court`() {
        assertFalse(AnswerChecker.isTypedTextCorrect(expected, "Or le foi est une ferme assurance des choses qu'on espère"))
    }

    @Test
    fun `refuse un mot oublié`() {
        assertFalse(AnswerChecker.isTypedTextCorrect(expected, "Or la foi est une assurance des choses qu'on espère"))
    }

    @Test
    fun `vérifie l'ordre des blocs par leur texte`() {
        val exercise = Exercise.Reorder(verse("v1"), chunks = listOf("et ce", "vient", "et ce"), shuffledOrder = listOf(2, 1, 0))

        assertTrue(AnswerChecker.isCorrect(exercise, ExerciseAnswer.Order(listOf(2, 1, 0))))
        assertFalse(AnswerChecker.isCorrect(exercise, ExerciseAnswer.Order(listOf(1, 0, 2))))
    }

    @Test
    fun `vérifie les mots manquants sans tenir compte des majuscules`() {
        val exercise = Exercise.FillBlanks(verse("v1"), words = listOf("Ainsi", "la", "foi", "vient"), blankIndexes = listOf(0, 3), choices = listOf("vient", "ainsi", "parole"))

        assertTrue(AnswerChecker.isCorrect(exercise, ExerciseAnswer.Blanks(listOf("ainsi", "vient"))))
        assertFalse(AnswerChecker.isCorrect(exercise, ExerciseAnswer.Blanks(listOf("vient", "ainsi"))))
    }

    @Test
    fun `les trous écrits acceptent une faute de frappe et ignorent les accents`() {
        val exercise = Exercise.TypeBlanks(verse("v1"), words = listOf("Or", "la", "foi", "est", "une", "ferme", "assurance"), blankIndexes = listOf(5, 6))

        assertTrue(AnswerChecker.isCorrect(exercise, ExerciseAnswer.Blanks(listOf("Ferme", "asurance"))))
        assertFalse(AnswerChecker.isCorrect(exercise, ExerciseAnswer.Blanks(listOf("forte", "assurance"))))
    }

    @Test
    fun `l'oral accepte quelques mots mal transcrits`() {
        val spoken = "or la foi est une ferme assurance des choses con espère une démonstration de celles con ne voit pas"

        assertTrue(AnswerChecker.isRecitationCorrect(LONG_VERSE, spoken))
    }

    @Test
    fun `l'oral refuse une récitation incomplète`() {
        assertFalse(AnswerChecker.isRecitationCorrect(LONG_VERSE, "or la foi est une ferme assurance"))
    }

    @Test
    fun `réciter en écrivant à la place de l'oral exige tous les mots`() {
        // Régression QA : un mot oublié était accepté en mode « Écrire plutôt ».
        val exercise = Exercise.ReciteAloud(verse("v1", text = LONG_VERSE))
        val missingWord = "Or la foi est une ferme assurance des choses qu’on espère, une démonstration de celles qu’on voit pas."

        assertFalse(AnswerChecker.isCorrect(exercise, ExerciseAnswer.Typed(missingWord)))
        assertTrue(AnswerChecker.isCorrect(exercise, ExerciseAnswer.Spoken(missingWord)))
        assertTrue(AnswerChecker.isCorrect(exercise, ExerciseAnswer.Typed(LONG_VERSE)))
    }

    private companion object {
        const val LONG_VERSE = "Or la foi est une ferme assurance des choses qu’on espère, une démonstration de celles qu’on ne voit pas."
    }
}
