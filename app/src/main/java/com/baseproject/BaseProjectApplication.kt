package com.baseproject

import android.app.Application
import com.baseproject.core.di.eagerModule
import com.baseproject.core.di.firebaseModule
import com.baseproject.core.di.lazyModule
import com.baseproject.speech.di.speechModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.lazyModules
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class BaseProjectApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(this@BaseProjectApplication)
            modules(eagerModule, firebaseModule, speechModule)
            lazyModules(lazyModule)
        }
    }
}
