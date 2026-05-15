package com.baseproject.presentation.feature.speechtest

import com.baseproject.presentation.base.UiEffect
import com.baseproject.presentation.base.UiEvent
import com.baseproject.presentation.base.UiState

data class SpeechTestState(
    val selectedTab: SpeechTab = SpeechTab.STT,
    // STT
    val isListening: Boolean = false,
    val sttResult: String = "",
    val sttPartial: String = "",
    val sttConfidence: Float = -1f,
    val sttAlternatives: List<String> = emptyList(),
    val sttError: String? = null,
    // TTS
    val ttsInput: String = "Hello, welcome to the language learning app.",
    val isSpeaking: Boolean = false,
    val ttsInitialized: Boolean = false,
    val ttsError: String? = null,
    // Pronunciation
    val pronReference: String = "the cat sat on the mat",
    val pronSpoken: String = "",
    val pronScore: Float? = null,
    val pronLevel: String = "",
    val pronWordMatchScore: Float? = null,
    val pronOrderScore: Float? = null,
    val pronWordDetails: List<PronWordUi> = emptyList(),
    val pronError: String? = null,
) : UiState

data class PronWordUi(
    val word: String,
    val isMatched: Boolean,
    val similarity: Float,
)

enum class SpeechTab(val label: String) {
    STT("STT"),
    TTS("TTS"),
    PRONUNCIATION("Pronunciation"),
}

sealed interface SpeechTestEvent : UiEvent {
    data class TabSelected(val tab: SpeechTab) : SpeechTestEvent
    // STT
    data object StartListening : SpeechTestEvent
    data object StopListening : SpeechTestEvent
    // TTS
    data class TtsInputChanged(val value: String) : SpeechTestEvent
    data object Speak : SpeechTestEvent
    data object StopSpeaking : SpeechTestEvent
    // Pronunciation
    data class PronReferenceChanged(val value: String) : SpeechTestEvent
    data object StartPronTest : SpeechTestEvent
}

sealed interface SpeechTestEffect : UiEffect {
    data class ShowMessage(val message: String) : SpeechTestEffect
}
