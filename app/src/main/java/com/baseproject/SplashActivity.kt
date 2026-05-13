package com.baseproject

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.baseproject.data.firebase.RemoteConfigHelper
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.android.ext.android.inject

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    private val remoteConfig: FirebaseRemoteConfig by inject()
    private var isReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { !isReady }

        lifecycleScope.launch {
            withTimeoutOrNull(SPLASH_TIMEOUT_MS) {
                RemoteConfigHelper.fetchAndActivate(remoteConfig)
            }
            isReady = true
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }

    companion object {
        private const val SPLASH_TIMEOUT_MS = 4_000L
    }
}
