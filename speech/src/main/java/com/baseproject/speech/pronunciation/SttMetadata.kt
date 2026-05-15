package com.baseproject.speech.pronunciation

data class SttMetadata(
    val confidence: Float = -1f,
    val alternatives: List<String> = emptyList(),
)
