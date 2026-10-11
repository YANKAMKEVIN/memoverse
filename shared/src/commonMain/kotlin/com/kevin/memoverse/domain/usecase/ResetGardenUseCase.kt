package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.repository.LearningRepository

/** Efface tous les versets plantés et leur progression. */
class ResetGardenUseCase(
    private val repository: LearningRepository,
) {
    suspend operator fun invoke(): DataResult<Unit> = repository.clear()
}
