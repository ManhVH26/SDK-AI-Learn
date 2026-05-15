package com.baseproject.presentation.feature.speechtest

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object SpeechTest

fun NavGraphBuilder.speechTestScreen(
    onBack: () -> Unit,
) {
    composable<SpeechTest> {
        SpeechTestScreen(
            viewModel = koinViewModel(),
            onBack = onBack,
        )
    }
}
