package com.baseproject.presentation.feature.home

import com.baseproject.presentation.base.UiEffect
import com.baseproject.presentation.base.UiEvent
import com.baseproject.presentation.base.UiState

data class HomeState(
    val isLoading: Boolean = false,
    val greeting: String = "",
    val greetingInput: String = "",
    val errorMessage: String? = null,
) : UiState

sealed interface HomeEvent : UiEvent {
    data object LoadGreeting : HomeEvent
    data object PrimaryClicked : HomeEvent
    data class GreetingInputChanged(val value: String) : HomeEvent
    data object SaveGreeting : HomeEvent
}

sealed interface HomeEffect : UiEffect {
    data class ShowMessage(val message: String) : HomeEffect
    data object NavigateNext : HomeEffect
}
