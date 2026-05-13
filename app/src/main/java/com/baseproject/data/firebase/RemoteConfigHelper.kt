package com.baseproject.data.firebase

import com.baseproject.BuildConfig
import com.baseproject.domain.repository.RemoteConfigRepository.Keys
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.tasks.await

object RemoteConfigHelper {

    // ── Generic getters ──────────────────────────────────────────────────────

    fun getString(config: FirebaseRemoteConfig, key: String): String =
        config.getString(key)

    fun getLong(config: FirebaseRemoteConfig, key: String): Long =
        config.getLong(key)

    fun getBoolean(config: FirebaseRemoteConfig, key: String): Boolean =
        config.getBoolean(key)

    suspend fun fetchAndActivate(config: FirebaseRemoteConfig): Boolean {
        return try {
            config.fetchAndActivate().await()
        } catch (_: Exception) {
            false
        }
    }

    // ── Defaults ─────────────────────────────────────────────────────────────

    val DEFAULTS: Map<String, Any> = mapOf(
        Keys.KEY_FORCE_UPDATE_ENABLED to false,
        Keys.KEY_FORCE_UPDATE_VERSION to "",
        Keys.KEY_MAINTENANCE_MODE to false,
    )

    // ── Settings ─────────────────────────────────────────────────────────────

    fun buildSettings() = remoteConfigSettings {
        minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0L else 3_600L
    }
}
