package com.baseproject.core.di

import com.baseproject.presentation.feature.speechtest.SpeechTestViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.lazyModule as koinLazyModule

// Reserved for bindings that are NOT on the launch path.
// Loaded by Koin in a background coroutine via startKoin { lazyModules(lazyModule) }.
val lazyModule = koinLazyModule {
    // ============ Data module ============

    // ============ Repository module ============

    // ============ UseCase module ============

    // ============ ViewModel module ============
    viewModelOf(::SpeechTestViewModel)
}
