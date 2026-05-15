package com.baseproject.speech.tts

sealed interface TtsEvent {
    data class Start(val utteranceId: String) : TtsEvent
    data class Done(val utteranceId: String) : TtsEvent
    data class Error(val utteranceId: String, val code: TtsErrorCode) : TtsEvent
}

enum class TtsErrorCode {
    SYNTHESIS,
    NETWORK,
    NOT_INSTALLED_YET,
    OUTPUT,
    SERVICE,
    UNKNOWN,
}
