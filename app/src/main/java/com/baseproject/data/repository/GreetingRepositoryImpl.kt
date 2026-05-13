package com.baseproject.data.repository

import com.baseproject.data.local.datastore.AppPreferences
import com.baseproject.data.local.datastore.PreferenceKeys
import com.baseproject.domain.model.Greeting
import com.baseproject.domain.repository.GreetingRepository
import kotlinx.coroutines.flow.first

class GreetingRepositoryImpl(
    private val preferences: AppPreferences,
) : GreetingRepository {

    override suspend fun getGreeting(name: String): Greeting {
        val stored = preferences.observeNullable(PreferenceKeys.GREETING_MESSAGE).first()
        return Greeting(message = stored ?: "Hello, $name!")
    }

    override suspend fun saveGreeting(message: String) {
        preferences.put(PreferenceKeys.GREETING_MESSAGE, message)
    }
}
