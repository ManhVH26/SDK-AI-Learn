package com.baseproject.speech.tts

import java.util.Locale

data class TtsConfig(
    val locale: Locale = Locale.US,
    val pitch: Float = 1.0f,
    val speechRate: Float = 0.95f,
    val voiceIndex: Int? = null,
)

enum class TtsSpeed(val rate: Float) {
    NORMAL(0.95f),
    SLOW(0.7f),
    VERY_SLOW(0.45f),
}
