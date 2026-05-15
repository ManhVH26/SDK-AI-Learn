package com.baseproject.speech.tts.android

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.baseproject.speech.tts.QueueMode
import com.baseproject.speech.tts.TextToSpeechProvider
import com.baseproject.speech.tts.TtsConfig
import com.baseproject.speech.tts.TtsErrorCode
import com.baseproject.speech.tts.TtsEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Locale
import java.util.UUID

class AndroidTextToSpeechProvider(
    private val context: Context,
) : TextToSpeechProvider() {

    private val _events = MutableSharedFlow<TtsEvent>(extraBufferCapacity = 16)
    override val events: Flow<TtsEvent> = _events.asSharedFlow()

    private var tts: TextToSpeech? = null
    private var currentConfig = TtsConfig()
    private var initialized = false

    override val isSpeaking: Boolean
        get() = tts?.isSpeaking == true

    override fun initialize(config: TtsConfig) {
        currentConfig = config
        tts = TextToSpeech(context, { status ->
            if (status == TextToSpeech.SUCCESS) {
                initialized = true
                applyConfig(config)
                setupListener()
                tts?.speak("", TextToSpeech.QUEUE_FLUSH, null, "preload")
            }
        }, GOOGLE_TTS_ENGINE)
    }

    override fun speak(text: String, queueMode: QueueMode) {
        if (!initialized) return
        val cleaned = cleanTextForTts(text, currentConfig.locale)
        val id = UUID.randomUUID().toString()
        val mode = when (queueMode) {
            QueueMode.FLUSH -> TextToSpeech.QUEUE_FLUSH
            QueueMode.ADD -> TextToSpeech.QUEUE_ADD
        }
        tts?.speak(cleaned, mode, null, id)
    }

    override fun speakSequence(texts: List<String>) {
        if (!initialized || texts.isEmpty()) return
        texts.forEachIndexed { index, text ->
            val mode = if (index == 0) QueueMode.FLUSH else QueueMode.ADD
            speak(text, mode)
        }
    }

    override fun stop() {
        tts?.stop()
    }

    override fun updateConfig(config: TtsConfig) {
        currentConfig = config
        if (initialized) applyConfig(config)
    }

    override fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        initialized = false
    }

    override fun isLanguageAvailable(config: TtsConfig): Boolean {
        val result = tts?.isLanguageAvailable(config.locale) ?: return false
        return result >= TextToSpeech.LANG_AVAILABLE
    }

    fun isGoogleTtsEngine(): Boolean =
        tts?.defaultEngine == GOOGLE_TTS_ENGINE

    private fun applyConfig(config: TtsConfig) {
        tts?.apply {
            language = config.locale
            setPitch(config.pitch)
            setSpeechRate(config.speechRate)
            config.voiceIndex?.let { index ->
                val voices = voices?.filter { it.locale == config.locale }?.toList()
                if (voices != null && index < voices.size) {
                    voice = voices[index]
                }
            }
        }
    }

    private fun setupListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                _events.tryEmit(TtsEvent.Start(utteranceId))
            }

            override fun onDone(utteranceId: String) {
                _events.tryEmit(TtsEvent.Done(utteranceId))
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String) {
                _events.tryEmit(TtsEvent.Error(utteranceId, TtsErrorCode.UNKNOWN))
            }

            override fun onError(utteranceId: String, errorCode: Int) {
                _events.tryEmit(TtsEvent.Error(utteranceId, mapErrorCode(errorCode)))
            }
        })
    }

    private fun cleanTextForTts(text: String, locale: Locale): String {
        var cleaned = text
            .replace(Regex("[*_~`#]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        val cjkLanguages = setOf("zh", "ja", "ko")
        if (locale.language in cjkLanguages) {
            cleaned = cleaned.replace(Regex("[()\\[\\]{}]"), "")
        }
        return cleaned
    }

    private fun mapErrorCode(errorCode: Int): TtsErrorCode = when (errorCode) {
        TextToSpeech.ERROR_SYNTHESIS -> TtsErrorCode.SYNTHESIS
        TextToSpeech.ERROR_NETWORK, TextToSpeech.ERROR_NETWORK_TIMEOUT -> TtsErrorCode.NETWORK
        TextToSpeech.ERROR_NOT_INSTALLED_YET -> TtsErrorCode.NOT_INSTALLED_YET
        TextToSpeech.ERROR_OUTPUT -> TtsErrorCode.OUTPUT
        TextToSpeech.ERROR_SERVICE -> TtsErrorCode.SERVICE
        else -> TtsErrorCode.UNKNOWN
    }

    companion object {
        private const val GOOGLE_TTS_ENGINE = "com.google.android.tts"
    }
}
