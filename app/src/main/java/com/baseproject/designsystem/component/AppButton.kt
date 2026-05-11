package com.baseproject.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.baseproject.designsystem.theme.AppTheme

enum class AppButtonStyle { Primary, Secondary, Text }

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppButtonStyle = AppButtonStyle.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val isEnabled = enabled && !loading
    when (style) {
        AppButtonStyle.Primary -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = isEnabled,
            shape = AppTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.primary,
                contentColor = AppTheme.colors.onPrimary,
            ),
        ) { ButtonContent(text = text, loading = loading) }

        AppButtonStyle.Secondary -> OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = isEnabled,
            shape = AppTheme.shapes.medium,
        ) { ButtonContent(text = text, loading = loading) }

        AppButtonStyle.Text -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = isEnabled,
        ) { ButtonContent(text = text, loading = loading) }
    }
}

@Composable
private fun ButtonContent(text: String, loading: Boolean) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
        )
    } else {
        AppText(text = text, style = AppTheme.typography.labelLarge)
    }
}
