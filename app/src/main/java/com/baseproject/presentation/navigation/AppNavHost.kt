package com.baseproject.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.baseproject.presentation.feature.home.Home
import com.baseproject.presentation.feature.home.homeScreen
import com.baseproject.presentation.feature.speechtest.SpeechTest
import com.baseproject.presentation.feature.speechtest.speechTestScreen

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Home,
        modifier = modifier,
    ) {
        homeScreen(
            onNavigateNext = { navController.navigate(SpeechTest) },
        )
        speechTestScreen(
            onBack = { navController.popBackStack() },
        )
    }
}
