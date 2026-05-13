package com.baseproject.domain.repository

interface RemoteConfigRepository {
    fun getBoolean(key: String): Boolean
    fun getString(key: String): String
    fun getLong(key: String): Long

    companion object Keys {
        const val KEY_FORCE_UPDATE_ENABLED = "force_update_enabled"
        const val KEY_FORCE_UPDATE_VERSION = "force_update_version"
        const val KEY_MAINTENANCE_MODE = "maintenance_mode"
    }
}
