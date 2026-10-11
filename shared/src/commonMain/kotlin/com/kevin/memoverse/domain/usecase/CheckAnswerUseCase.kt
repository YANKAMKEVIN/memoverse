package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.exercise.AnswerChecker
import com.kevin.memoverse.domain.model.Exercise
import com.kevin.memoverse.domain.model.ExerciseAnswer

class CheckAnswerUseCase {
    operator fun invoke(exercise: Exercise, answer: ExerciseAnswer): Boolean = AnswerChecker.isCorrect(exercise, answer)
}
