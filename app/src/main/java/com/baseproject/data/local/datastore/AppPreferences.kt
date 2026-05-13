package com.baseproject.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

interface AppPreferences {
    fun <T> observe(key: Preferences.Key<T>, default: T): Flow<T>
    fun <T> observeNullable(key: Preferences.Key<T>, default: T? = null): Flow<T?>

    suspend fun <T> put(key: Preferences.Key<T>, value: T)
    suspend fun <T> remove(key: Preferences.Key<T>)
    suspend fun clear()
}

class AppPreferencesImpl(
    private val dataStore: DataStore<Preferences>,
) : AppPreferences {

    override fun <T> observe(key: Preferences.Key<T>, default: T): Flow<T> =
        dataStore.data.safe().map { it[key] ?: default }

    override fun <T> observeNullable(key: Preferences.Key<T>, default: T?): Flow<T?> =
        dataStore.data.safe().map { it[key] ?: default }

    override suspend fun <T> put(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }

    override suspend fun <T> remove(key: Preferences.Key<T>) {
        dataStore.edit { it.remove(key) }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private fun Flow<Preferences>.safe(): Flow<Preferences> = catch { cause ->
        if (cause is IOException) emit(emptyPreferences())
        else throw cause
    }
}
