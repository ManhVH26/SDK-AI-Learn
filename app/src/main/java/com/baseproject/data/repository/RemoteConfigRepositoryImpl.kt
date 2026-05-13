package com.baseproject.data.repository

import com.baseproject.data.firebase.RemoteConfigHelper
import com.baseproject.domain.repository.RemoteConfigRepository
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class RemoteConfigRepositoryImpl(
    private val remoteConfig: FirebaseRemoteConfig,
) : RemoteConfigRepository {

    override fun getBoolean(key: String): Boolean =
        RemoteConfigHelper.getBoolean(remoteConfig, key)

    override fun getString(key: String): String =
        RemoteConfigHelper.getString(remoteConfig, key)

    override fun getLong(key: String): Long =
        RemoteConfigHelper.getLong(remoteConfig, key)
}
