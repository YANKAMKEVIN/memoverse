package com.kevin.memoverse.domain.model

sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Error(val cause: Throwable) : DataResult<Nothing>
}
