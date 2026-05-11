package com.baseproject.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<S : UiState, E : UiEvent, F : UiEffect>(
    initialState: S,
) : ViewModel() {

    private val _state: MutableStateFlow<S> = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _event: MutableSharedFlow<E> = MutableSharedFlow(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val event: Flow<E> = _event.asSharedFlow()

    private val _effect: Channel<F> = Channel(Channel.BUFFERED)
    val effect: Flow<F> = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            _event.collect { handleEvent(it) }
        }
    }

    fun sendEvent(event: E) {
        _event.tryEmit(event)
    }

    protected fun setState(reducer: S.() -> S) {
        _state.value = _state.value.reducer()
    }

    protected fun sendEffect(effect: F) {
        viewModelScope.launch { _effect.send(effect) }
    }

    protected abstract fun handleEvent(event: E)
}
