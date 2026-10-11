package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.ThemeFamily
import com.kevin.memoverse.domain.repository.VerseBankRepository

class GetThemeFamiliesUseCase(
    private val repository: VerseBankRepository,
) {
    suspend operator fun invoke(): DataResult<List<ThemeFamily>> =
        when (val result = repository.getThemes()) {
            is DataResult.Success -> DataResult.Success(
                // groupBy garde l'ordre de première apparition : l'ordre des familles suit la banque.
                result.data.groupBy { it.family }.map { (family, themes) -> ThemeFamily(family, themes) },
            )
            is DataResult.Error -> result
        }
}
