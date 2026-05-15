package com.baseproject.presentation.feature.speechtest

import androidx.lifecycle.viewModelScope
import com.baseproject.presentation.base.BaseViewModel
import com.baseproject.speech.pronunciation.PronunciationAssessor
import com.baseproject.speech.pronunciation.SttMetadata
import com.baseproject.speech.stt.SpeechToTextProvider
import com.baseproject.speech.stt.SttEvent
import com.baseproject.speech.tts.TextToSpeechProvider
import com.baseproject.speech.tts.TtsEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SpeechTestViewModel(
    private val sttProvider: SpeechToTextProvider,
    private val ttsProvider: TextToSpeechProvider,
    private val pronunciationAssessor: PronunciationAssessor,
) : BaseViewModel<SpeechTestState, SpeechTestEvent, SpeechTestEffect>(SpeechTestState()) {

    private var sttCollectJob: Job? = null
    private var ttsCollectJob: Job? = null

    init {
        ttsProvider.initialize()
        setState { copy(ttsInitialized = true) }
        collectTtsEvents()
    }

    override fun handleEvent(event: SpeechTestEvent) {
        when (event) {
            is SpeechTestEvent.TabSelected -> setState { copy(selectedTab = event.tab) }
            SpeechTestEvent.StartListening -> startListening()
            SpeechTestEvent.StopListening -> stopListening()
            is SpeechTestEvent.TtsInputChanged -> setState { copy(ttsInput = event.value) }
            SpeechTestEvent.Speak -> speak()
            SpeechTestEvent.StopSpeaking -> stopSpeaking()
            is SpeechTestEvent.PronReferenceChanged -> setState { copy(pronReference = event.value) }
            SpeechTestEvent.StartPronTest -> startPronTest()
        }
    }

    private fun startListening() {
        setState {
            copy(
                isListening = true,
                sttResult = "",
                sttPartial = "",
                sttConfidence = -1f,
                sttAlternatives = emptyList(),
                sttError = null,
            )
        }
        collectSttEvents()
        sttProvider.startListening()
    }

    private fun stopListening() {
        sttProvider.stopListening()
        setState { copy(isListening = false) }
    }

    private fun collectSttEvents() {
        sttCollectJob?.cancel()
        sttCollectJob = viewModelScope.launch {
            sttProvider.events.collect { event ->
                when (event) {
                    is SttEvent.Result -> setState {
                        copy(
                            sttResult = event.text,
                            sttConfidence = event.confidence,
                            sttAlternatives = event.alternatives,
                            sttPartial = "",
                            isListening = false,
                        )
                    }
                    is SttEvent.PartialResult -> setState { copy(sttPartial = event.text) }
                    is SttEvent.Error -> {
                        setState { copy(sttError = "${event.code}: ${event.message}", isListening = false) }
                    }
                    SttEvent.ReadyForSpeech -> Unit
                    SttEvent.BeginningOfSpeech -> Unit
                    SttEvent.EndOfSpeech -> setState { copy(isListening = false) }
                }
            }
        }
    }

    private fun collectTtsEvents() {
        ttsCollectJob?.cancel()
        ttsCollectJob = viewModelScope.launch {
            ttsProvider.events.collect { event ->
                when (event) {
                    is TtsEvent.Start -> setState { copy(isSpeaking = true, ttsError = null) }
                    is TtsEvent.Done -> setState { copy(isSpeaking = false) }
                    is TtsEvent.Error -> setState {
                        copy(isSpeaking = false, ttsError = event.code.name)
                    }
                }
            }
        }
    }

    private fun speak() {
        val text = state.value.ttsInput.trim()
        if (text.isEmpty()) return
        ttsProvider.speak(text)
    }

    private fun stopSpeaking() {
        ttsProvider.stop()
        setState { copy(isSpeaking = false) }
    }

    private fun startPronTest() {
        setState {
            copy(
                pronSpoken = "",
                pronScore = null,
                pronLevel = "",
                pronWordMatchScore = null,
                pronOrderScore = null,
                pronWordDetails = emptyList(),
                pronError = null,
                isListening = true,
            )
        }
        collectPronSttEvents()
        sttProvider.startListening()
    }

    private fun collectPronSttEvents() {
        sttCollectJob?.cancel()
        sttCollectJob = viewModelScope.launch {
            sttProvider.events.collect { event ->
                when (event) {
                    is SttEvent.Result -> {
                        setState { copy(pronSpoken = event.text, isListening = false) }
                        assessPronunciation(
                            spokenText = event.text,
                            confidence = event.confidence,
                            alternatives = event.alternatives,
                        )
                    }
                    is SttEvent.PartialResult -> setState { copy(pronSpoken = event.text) }
                    is SttEvent.Error -> {
                        setState { copy(pronError = "${event.code}: ${event.message}", isListening = false) }
                    }
                    SttEvent.EndOfSpeech -> setState { copy(isListening = false) }
                    else -> Unit
                }
            }
        }
    }

    private fun assessPronunciation(
        spokenText: String,
        confidence: Float,
        alternatives: List<String>,
    ) {
        val result = pronunciationAssessor.assess(
            referenceText = state.value.pronReference,
            spokenText = spokenText,
            sttMetadata = SttMetadata(
                confidence = confidence,
                alternatives = alternatives,
            ),
        )
        setState {
            copy(
                pronScore = result.score,
                pronLevel = result.level.label,
                pronWordMatchScore = result.wordMatchScore,
                pronOrderScore = result.orderScore,
                pronWordDetails = result.wordDetails.map {
                    PronWordUi(word = it.word, isMatched = it.isMatched, similarity = it.similarity)
                },
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        sttCollectJob?.cancel()
        ttsCollectJob?.cancel()
        sttProvider.destroy()
        ttsProvider.destroy()
    }
}
