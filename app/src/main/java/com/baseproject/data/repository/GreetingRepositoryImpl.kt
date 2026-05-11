package com.baseproject.data.repository

import com.baseproject.domain.model.Greeting
import com.baseproject.domain.repository.GreetingRepository
import kotlinx.coroutines.delay

class GreetingRepositoryImpl : GreetingRepository {
    override suspend fun getGreeting(name: String): Greeting {
        delay(300)
        return Greeting(message = "Hello, $name!")
    }
}
