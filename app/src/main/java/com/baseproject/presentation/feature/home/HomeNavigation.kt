package com.baseproject.presentation.feature.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object Home

fun NavGraphBuilder.homeScreen(
    onNavigateNext: () -> Unit,
) {
    composable<Home> {
        HomeScreen(
            viewModel = koinViewModel(),
            onNavigateNext = onNavigateNext,
        )
    }
}
