package com.baseproject.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppSpacing(
    val none: Dp,
    val xxs: Dp,
    val xs: Dp,
    val s: Dp,
    val m: Dp,
    val l: Dp,
    val xl: Dp,
    val xxl: Dp,
    val xxxl: Dp,
)

val DefaultAppSpacing = AppSpacing(
    none = 0.dp,
    xxs = 2.dp,
    xs = 4.dp,
    s = 8.dp,
    m = 12.dp,
    l = 16.dp,
    xl = 24.dp,
    xxl = 32.dp,
    xxxl = 48.dp,
)

val LocalAppSpacing = staticCompositionLocalOf<AppSpacing> { DefaultAppSpacing }
