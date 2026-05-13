package com.baseproject.presentation.feature.home

import androidx.lifecycle.viewModelScope
import com.baseproject.core.common.Result
import com.baseproject.domain.repository.RemoteConfigRepository
import com.baseproject.domain.repository.RemoteConfigRepository.Keys
import com.baseproject.domain.usecase.GetGreetingUseCase
import com.baseproject.domain.usecase.SaveGreetingUseCase
import com.baseproject.presentation.base.BaseViewModel
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getGreetingUseCase: GetGreetingUseCase,
    private val saveGreetingUseCase: SaveGreetingUseCase,
    private val remoteConfigRepository: RemoteConfigRepository,
) : BaseViewModel<HomeState, HomeEvent, HomeEffect>(HomeState()) {

    init {
        loadRemoteConfig()
        sendEvent(HomeEvent.LoadGreeting)
    }

    private fun loadRemoteConfig() {
        setState {
            copy(
                remoteConfig = RemoteConfigState(
                    forceUpdateEnabled = remoteConfigRepository.getBoolean(Keys.KEY_FORCE_UPDATE_ENABLED),
                    forceUpdateVersion = remoteConfigRepository.getString(Keys.KEY_FORCE_UPDATE_VERSION),
                    maintenanceMode = remoteConfigRepository.getBoolean(Keys.KEY_MAINTENANCE_MODE),
                )
            )
        }
    }

    override fun handleEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.LoadGreeting -> loadGreeting()
            is HomeEvent.PrimaryClicked -> sendEffect(HomeEffect.NavigateNext)
            is HomeEvent.GreetingInputChanged -> setState { copy(greetingInput = event.value) }
            is HomeEvent.SaveGreeting -> saveGreeting()
        }
    }

    private fun loadGreeting() {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            when (val result = getGreetingUseCase("BaseProject")) {
                is Result.Success -> setState {
                    copy(isLoading = false, greeting = result.data.message)
                }
                is Result.Failure -> {
                    setState { copy(isLoading = false, errorMessage = result.error.message) }
                    sendEffect(HomeEffect.ShowMessage(result.error.message ?: "Unknown error"))
                }
                Result.Loading -> setState { copy(isLoading = true) }
            }
        }
    }

    private fun saveGreeting() {
        val input = state.value.greetingInput.trim()
        if (input.isEmpty()) return
        viewModelScope.launch {
            when (val result = saveGreetingUseCase(input)) {
                is Result.Success -> {
                    setState { copy(greetingInput = "") }
                    sendEffect(HomeEffect.ShowMessage("Saved!"))
                    loadGreeting()
                }
                is Result.Failure -> {
                    sendEffect(HomeEffect.ShowMessage(result.error.message ?: "Save failed"))
                }
                Result.Loading -> Unit
            }
        }
    }
}
