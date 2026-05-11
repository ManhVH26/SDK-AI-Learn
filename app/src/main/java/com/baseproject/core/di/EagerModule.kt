package com.baseproject.core.di

import com.baseproject.core.common.DefaultDispatcherProvider
import com.baseproject.core.common.DispatcherProvider
import com.baseproject.data.repository.GreetingRepositoryImpl
import com.baseproject.domain.repository.GreetingRepository
import com.baseproject.domain.usecase.GetGreetingUseCase
import com.baseproject.presentation.feature.home.HomeViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

// Cross-cutting deps + features on the launch path (Home is the start destination)
// Avoid createdAtStart() — let Koin resolve on first inject.
val eagerModule = module {
    // ============ Core / cross-cutting ============
    singleOf(::DefaultDispatcherProvider) { bind<DispatcherProvider>() }

    // ============ Data module ============

    // ============ Repository module ============
    singleOf(::GreetingRepositoryImpl) { bind<GreetingRepository>() }

    // ============ UseCase module ============
    factoryOf(::GetGreetingUseCase)

    // ============ ViewModel module ============
    viewModelOf(::HomeViewModel)
}
