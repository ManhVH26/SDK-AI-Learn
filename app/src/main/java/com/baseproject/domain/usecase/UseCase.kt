package com.baseproject.domain.usecase

import com.baseproject.core.common.DispatcherProvider
import com.baseproject.core.common.Result
import com.baseproject.core.common.runCatchingResult
import kotlinx.coroutines.withContext

abstract class UseCase<in P, R>(
    private val dispatcherProvider: DispatcherProvider,
) {
    suspend operator fun invoke(params: P): Result<R> =
        withContext(dispatcherProvider.io) {
            runCatchingResult { execute(params) }
        }

    protected abstract suspend fun execute(params: P): R
}

abstract class NoParamsUseCase<R>(
    dispatcherProvider: DispatcherProvider,
) : UseCase<Unit, R>(dispatcherProvider) {
    suspend operator fun invoke(): Result<R> = invoke(Unit)
}
