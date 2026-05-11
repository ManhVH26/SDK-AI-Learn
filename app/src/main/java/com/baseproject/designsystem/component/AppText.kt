package com.baseproject.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.baseproject.designsystem.theme.AppTheme

@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = AppTheme.typography.bodyMedium,
) {
    Text(
        text = text,
        modifier = modifier,
        color = if (color == Color.Unspecified) AppTheme.colors.onBackground else color,
        style = style,
    )
}
