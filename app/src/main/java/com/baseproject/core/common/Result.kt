package com.baseproject.core.common

import kotlinx.coroutines.CancellationException

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Failure(val error: Throwable) : Result<Nothing>
    data object Loading : Result<Nothing>
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Failure -> this
    is Result.Loading -> this
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onFailure(action: (Throwable) -> Unit): Result<T> {
    if (this is Result.Failure) action(error)
    return this
}

inline fun <R> runCatchingResult(block: () -> R): Result<R> = try {
    Result.Success(block())
} catch (ce: CancellationException) {
    throw ce
} catch (t: Throwable) {
    Result.Failure(t)
}
