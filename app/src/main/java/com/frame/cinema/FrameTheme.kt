package com.frame.cinema

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal data class FrameColorPalette(
    val pageBackground: Color,
    val cardBackground: Color,
    val secondarySurface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val tertiaryText: Color,
    val divider: Color,
    val accent: Color,
    val danger: Color,
    val glass: Color
)

internal object FrameThemeColors {
    val Light = FrameColorPalette(
        pageBackground = Color(0xFFF5F5F7),
        cardBackground = Color.White,
        secondarySurface = Color(0xFFF1F4F4),
        primaryText = Color(0xFF1A1A1A),
        secondaryText = Color(0xFF888888),
        tertiaryText = Color(0xFFAAAAAA),
        divider = Color(0xFFF0F0F0),
        accent = Color(0xFF7FB5B1),
        danger = Color(0xFFFF3B30),
        glass = Color.White.copy(alpha = .85f)
    )

    val Dark = FrameColorPalette(
        pageBackground = Color(0xFF171B1B),
        cardBackground = Color(0xFF222929),
        secondarySurface = Color(0xFF2B3433),
        primaryText = Color(0xFFE6ECEA),
        secondaryText = Color(0xFFA0AAA7),
        tertiaryText = Color(0xFF788581),
        divider = Color(0xFF394340),
        accent = Color(0xFF83BDB5),
        danger = Color(0xFFFF6259),
        glass = Color(0xE6222929)
    )
}

internal val LocalFramePalette = staticCompositionLocalOf { FrameThemeColors.Light }

internal val PageBg: Color
    @Composable get() = LocalFramePalette.current.pageBackground
internal val MainText: Color
    @Composable get() = LocalFramePalette.current.primaryText
internal val ThemeTeal = FrameThemeColors.Light.accent
internal val ActiveAccent: Color
    @Composable get() = LocalFramePalette.current.accent
internal val MutedText: Color
    @Composable get() = LocalFramePalette.current.secondaryText
internal val Glass: Color
    @Composable get() = LocalFramePalette.current.glass

