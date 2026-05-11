package com.baseproject.presentation.feature.home

import androidx.lifecycle.viewModelScope
import com.baseproject.core.common.Result
import com.baseproject.domain.usecase.GetGreetingUseCase
import com.baseproject.presentation.base.BaseViewModel
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getGreetingUseCase: GetGreetingUseCase,
) : BaseViewModel<HomeState, HomeEvent, HomeEffect>(HomeState()) {

    init {
        sendEvent(HomeEvent.LoadGreeting)
    }

    override fun handleEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.LoadGreeting -> loadGreeting()
            HomeEvent.PrimaryClicked -> sendEffect(HomeEffect.NavigateNext)
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
}
