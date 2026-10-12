package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.exercise.AnswerChecker
import com.kevin.memoverse.domain.model.RecitationProgress

/** Où en est la récitation orale d'un verset, d'après ce qui a été entendu. */
class MeasureRecitationUseCase {
    operator fun invoke(verseText: String, spoken: String): RecitationProgress = AnswerChecker.recitationProgress(verseText, spoken)
}
