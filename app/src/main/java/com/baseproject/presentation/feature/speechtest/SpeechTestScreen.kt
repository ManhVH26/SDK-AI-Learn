package com.baseproject.presentation.feature.speechtest

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.baseproject.designsystem.component.AppButton
import com.baseproject.designsystem.component.AppButtonStyle
import com.baseproject.designsystem.component.AppText
import com.baseproject.designsystem.theme.AppTheme
import com.baseproject.presentation.base.CollectEffect

@Composable
fun SpeechTestScreen(
    viewModel: SpeechTestViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (state.selectedTab == SpeechTab.PRONUNCIATION) {
                viewModel.sendEvent(SpeechTestEvent.StartPronTest)
            } else {
                viewModel.sendEvent(SpeechTestEvent.StartListening)
            }
        } else {
            Toast.makeText(context, "Microphone permission required", Toast.LENGTH_SHORT).show()
        }
    }

    CollectEffect(viewModel.effect) { effect ->
        when (effect) {
            is SpeechTestEffect.ShowMessage ->
                Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.spacing.m, vertical = AppTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppButton(
                text = "Back",
                onClick = onBack,
                style = AppButtonStyle.Text,
            )
            AppText(
                text = "Speech Test",
                style = AppTheme.typography.titleLarge,
                modifier = Modifier.padding(start = AppTheme.spacing.s),
            )
        }

        TabRow(
            selectedTabIndex = state.selectedTab.ordinal,
        ) {
            SpeechTab.entries.forEach { tab ->
                Tab(
                    selected = state.selectedTab == tab,
                    onClick = { viewModel.sendEvent(SpeechTestEvent.TabSelected(tab)) },
                    text = { AppText(text = tab.label, style = AppTheme.typography.labelLarge) },
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppTheme.spacing.l),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
        ) {
            when (state.selectedTab) {
                SpeechTab.STT -> SttTab(
                    state = state,
                    onStartListening = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    onStopListening = { viewModel.sendEvent(SpeechTestEvent.StopListening) },
                )
                SpeechTab.TTS -> TtsTab(
                    state = state,
                    onInputChanged = { viewModel.sendEvent(SpeechTestEvent.TtsInputChanged(it)) },
                    onSpeak = { viewModel.sendEvent(SpeechTestEvent.Speak) },
                    onStop = { viewModel.sendEvent(SpeechTestEvent.StopSpeaking) },
                )
                SpeechTab.PRONUNCIATION -> PronunciationTab(
                    state = state,
                    onReferenceChanged = { viewModel.sendEvent(SpeechTestEvent.PronReferenceChanged(it)) },
                    onStartTest = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                )
            }
        }
    }
}

@Composable
private fun SttTab(
    state: SpeechTestState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
) {
    SectionLabel("Controls")

    if (state.isListening) {
        AppButton(
            text = "Stop Listening",
            onClick = onStopListening,
            style = AppButtonStyle.Secondary,
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        AppButton(
            text = "Start Listening",
            onClick = onStartListening,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    if (state.sttPartial.isNotEmpty()) {
        SectionLabel("Partial Result")
        AppText(
            text = state.sttPartial,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurfaceVariant,
        )
    }

    if (state.sttResult.isNotEmpty()) {
        SectionLabel("Final Result")
        AppText(text = state.sttResult, style = AppTheme.typography.bodyLarge)

        if (state.sttConfidence >= 0f) {
            AppText(
                text = "Confidence: %.2f".format(state.sttConfidence),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
            )
        }

        if (state.sttAlternatives.isNotEmpty()) {
            SectionLabel("Alternatives")
            state.sttAlternatives.forEachIndexed { i, alt ->
                AppText(text = "${i + 1}. $alt", style = AppTheme.typography.bodySmall)
            }
        }
    }

    state.sttError?.let {
        AppText(text = it, color = AppTheme.colors.error, style = AppTheme.typography.bodyMedium)
    }
}

@Composable
private fun TtsTab(
    state: SpeechTestState,
    onInputChanged: (String) -> Unit,
    onSpeak: () -> Unit,
    onStop: () -> Unit,
) {
    SectionLabel("Text to speak")

    OutlinedTextField(
        value = state.ttsInput,
        onValueChange = onInputChanged,
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 5,
    )

    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
        AppButton(
            text = if (state.isSpeaking) "Speaking..." else "Speak",
            onClick = onSpeak,
            enabled = !state.isSpeaking && state.ttsInput.isNotBlank(),
            modifier = Modifier.weight(1f),
        )
        if (state.isSpeaking) {
            AppButton(
                text = "Stop",
                onClick = onStop,
                style = AppButtonStyle.Secondary,
                modifier = Modifier.weight(1f),
            )
        }
    }

    state.ttsError?.let {
        AppText(text = "Error: $it", color = AppTheme.colors.error, style = AppTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PronunciationTab(
    state: SpeechTestState,
    onReferenceChanged: (String) -> Unit,
    onStartTest: () -> Unit,
) {
    SectionLabel("Reference text")

    OutlinedTextField(
        value = state.pronReference,
        onValueChange = onReferenceChanged,
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 3,
    )

    AppButton(
        text = if (state.isListening) "Listening..." else "Record & Assess",
        onClick = onStartTest,
        enabled = !state.isListening && state.pronReference.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    )

    if (state.pronSpoken.isNotEmpty()) {
        SectionLabel("You said")
        AppText(text = state.pronSpoken, style = AppTheme.typography.bodyLarge)
    }

    state.pronScore?.let { score ->
        HorizontalDivider(modifier = Modifier.padding(vertical = AppTheme.spacing.s))

        SectionLabel("Score: %.0f / 100 — %s".format(score, state.pronLevel))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            state.pronWordMatchScore?.let {
                AppText(text = "Word match: %.0f".format(it), style = AppTheme.typography.bodySmall)
            }
            state.pronOrderScore?.let {
                AppText(text = "Order: %.0f".format(it), style = AppTheme.typography.bodySmall)
            }
        }

        if (state.pronWordDetails.isNotEmpty()) {
            SectionLabel("Word details")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                state.pronWordDetails.forEach { detail ->
                    val bg = when {
                        detail.isMatched -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        detail.similarity >= 0.5f -> Color(0xFFFFC107).copy(alpha = 0.2f)
                        else -> Color(0xFFF44336).copy(alpha = 0.2f)
                    }
                    val textColor = when {
                        detail.isMatched -> Color(0xFF2E7D32)
                        detail.similarity >= 0.5f -> Color(0xFFF57F17)
                        else -> Color(0xFFC62828)
                    }
                    AppText(
                        text = "%s (%.0f%%)".format(detail.word, detail.similarity * 100),
                        style = AppTheme.typography.bodySmall,
                        color = textColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(bg)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }

    state.pronError?.let {
        AppText(text = it, color = AppTheme.colors.error, style = AppTheme.typography.bodyMedium)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Spacer(Modifier.height(AppTheme.spacing.xs))
    AppText(text = text, style = AppTheme.typography.titleMedium)
}
