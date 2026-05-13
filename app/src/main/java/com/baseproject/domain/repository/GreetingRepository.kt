package com.baseproject.domain.repository

import com.baseproject.domain.model.Greeting

interface GreetingRepository {
    suspend fun getGreeting(name: String): Greeting
    suspend fun saveGreeting(message: String)
}
