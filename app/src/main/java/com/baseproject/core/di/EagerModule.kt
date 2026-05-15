package com.baseproject.core.di

import androidx.room.Room
import com.baseproject.core.common.DefaultDispatcherProvider
import com.baseproject.core.common.DispatcherProvider
import com.baseproject.data.local.database.AppDatabase
import com.baseproject.data.local.datastore.AppPreferences
import com.baseproject.data.local.datastore.AppPreferencesImpl
import com.baseproject.data.local.datastore.appDataStore
import com.baseproject.data.repository.GreetingRepositoryImpl
import com.baseproject.data.repository.RemoteConfigRepositoryImpl
import com.baseproject.domain.repository.GreetingRepository
import com.baseproject.domain.repository.RemoteConfigRepository
import com.baseproject.domain.usecase.GetGreetingUseCase
import com.baseproject.domain.usecase.SaveGreetingUseCase
import com.baseproject.presentation.feature.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
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
    single { androidContext().appDataStore }
    singleOf(::AppPreferencesImpl) { bind<AppPreferences>() }
    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME,
        ).build()
    }

    // ============ Data module ============
    single { get<AppDatabase>().greetingDao() }

    // ============ Repository module ============
    singleOf(::GreetingRepositoryImpl) { bind<GreetingRepository>() }
    singleOf(::RemoteConfigRepositoryImpl) { bind<RemoteConfigRepository>() }

    // ============ UseCase module ============
    factoryOf(::GetGreetingUseCase)
    factoryOf(::SaveGreetingUseCase)

    // ============ ViewModel module ============
    viewModelOf(::HomeViewModel)
}
