package com.baseproject.speech.di

import com.baseproject.speech.pronunciation.PronunciationAssessor
import com.baseproject.speech.pronunciation.simple.SimpleWordMatchAssessor
import com.baseproject.speech.stt.SpeechToTextProvider
import com.baseproject.speech.stt.android.AndroidSpeechToTextProvider
import com.baseproject.speech.tts.TextToSpeechProvider
import com.baseproject.speech.tts.android.AndroidTextToSpeechProvider
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val speechModule = module {
    factoryOf(::AndroidSpeechToTextProvider) bind SpeechToTextProvider::class
    singleOf(::AndroidTextToSpeechProvider) bind TextToSpeechProvider::class
    singleOf(::SimpleWordMatchAssessor) bind PronunciationAssessor::class
}
