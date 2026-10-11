package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.LearningVerse
import com.kevin.memoverse.domain.repository.LearningRepository
import kotlinx.coroutines.flow.Flow

class ObserveLearningVersesUseCase(
    private val repository: LearningRepository,
) {
    operator fun invoke(): Flow<List<LearningVerse>> = repository.observeLearningVerses()
}
