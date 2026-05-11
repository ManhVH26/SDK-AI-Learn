package com.baseproject.presentation.feature.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.baseproject.designsystem.component.AppButton
import com.baseproject.designsystem.component.AppLoading
import com.baseproject.designsystem.component.AppText
import com.baseproject.designsystem.theme.AppTheme
import com.baseproject.presentation.base.CollectEffect

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateNext: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is HomeEffect.ShowMessage ->
                Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            HomeEffect.NavigateNext -> onNavigateNext()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppTheme.spacing.l),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.l, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            state.isLoading -> AppLoading()
            state.errorMessage != null -> AppText(
                text = state.errorMessage.orEmpty(),
                color = AppTheme.colors.error,
                style = AppTheme.typography.bodyLarge,
            )
            else -> AppText(
                text = state.greeting,
                style = AppTheme.typography.headlineMedium,
            )
        }

        AppButton(
            text = "Continue",
            onClick = { viewModel.sendEvent(HomeEvent.PrimaryClicked) },
            enabled = !state.isLoading,
        )
    }
}
