package com.baseproject.speech.tts

import kotlinx.coroutines.flow.Flow

abstract class TextToSpeechProvider {

    abstract val events: Flow<TtsEvent>

    abstract val isSpeaking: Boolean

    abstract fun initialize(config: TtsConfig = TtsConfig())

    abstract fun speak(text: String, queueMode: QueueMode = QueueMode.FLUSH)

    abstract fun speakSequence(texts: List<String>)

    abstract fun stop()

    abstract fun updateConfig(config: TtsConfig)

    abstract fun destroy()

    open fun isLanguageAvailable(config: TtsConfig): Boolean = true
}

enum class QueueMode { FLUSH, ADD }
