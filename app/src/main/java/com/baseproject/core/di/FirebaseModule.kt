package com.baseproject.core.di

import com.baseproject.data.firebase.RemoteConfigHelper
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.koin.dsl.module

val firebaseModule = module {
    single<FirebaseAnalytics> { FirebaseAnalytics.getInstance(get()) }
    single<FirebaseCrashlytics> { FirebaseCrashlytics.getInstance() }
    single<FirebaseRemoteConfig> {
        FirebaseRemoteConfig.getInstance().apply {
            setConfigSettingsAsync(RemoteConfigHelper.buildSettings())
            setDefaultsAsync(RemoteConfigHelper.DEFAULTS)
        }
    }
}
