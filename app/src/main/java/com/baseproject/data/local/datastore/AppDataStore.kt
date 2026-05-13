package com.baseproject.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val APP_PREFS_NAME = "app_prefs"

val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = APP_PREFS_NAME)
