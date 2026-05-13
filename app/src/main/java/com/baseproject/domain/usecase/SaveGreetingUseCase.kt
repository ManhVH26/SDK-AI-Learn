package com.baseproject.domain.usecase

import com.baseproject.core.common.DispatcherProvider
import com.baseproject.domain.repository.GreetingRepository

class SaveGreetingUseCase(
    private val repository: GreetingRepository,
    dispatcherProvider: DispatcherProvider,
) : UseCase<String, Unit>(dispatcherProvider) {
    override suspend fun execute(params: String) {
        repository.saveGreeting(params)
    }
}
