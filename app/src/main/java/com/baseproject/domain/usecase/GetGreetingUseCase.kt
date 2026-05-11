package com.baseproject.domain.usecase

import com.baseproject.core.common.DispatcherProvider
import com.baseproject.domain.model.Greeting
import com.baseproject.domain.repository.GreetingRepository

class GetGreetingUseCase(
    private val repository: GreetingRepository,
    dispatcherProvider: DispatcherProvider,
) : UseCase<String, Greeting>(dispatcherProvider) {
    override suspend fun execute(params: String): Greeting =
        repository.getGreeting(params)
}
