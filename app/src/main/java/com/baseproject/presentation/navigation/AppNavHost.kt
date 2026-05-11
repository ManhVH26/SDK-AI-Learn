package com.baseproject.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.baseproject.presentation.feature.home.Home
import com.baseproject.presentation.feature.home.homeScreen

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Home,
        modifier = modifier,
    ) {
        homeScreen(
            onNavigateNext = { /* no-op for now */ },
        )
    }
}
