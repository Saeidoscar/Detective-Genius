package com.detectivegenius.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

/**
 * Centralized Semantic Design Tokens for Detective Genius.
 * Never hardcode colors in screens.
 */
@Immutable
data class DetectiveColorTokens(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceHighlight: Color,
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val accentInvestigation: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val border: Color,
    val divider: Color,
    val disabled: Color,
    val cardBackground: Color,
    val packageAlphaColor: Color,
    val packageBetaColor: Color
)

val DarkColorTokens = DetectiveColorTokens(
    background = Color(0xFF0B1021),
    surface = Color(0xFF131B33),
    surfaceElevated = Color(0xFF1A2342),
    surfaceHighlight = Color(0xFF233058),
    primary = Color(0xFF8B5CF6),
    primaryVariant = Color(0xFF7C3AED),
    secondary = Color(0xFF38BDF8),
    accentInvestigation = Color(0xFF06B6D4),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFFCBD5E1),
    textMuted = Color(0xFF64748B),
    success = Color(0xFF10B981),
    warning = Color(0xFFF59E0B),
    error = Color(0xFFEF4444),
    border = Color(0xFF2A375E),
    divider = Color(0xFF1E293B),
    disabled = Color(0xFF334155),
    cardBackground = Color(0xFF161F3D),
    packageAlphaColor = Color(0xFF8B5CF6),
    packageBetaColor = Color(0xFF06B6D4)
)

val LightColorTokens = DetectiveColorTokens(
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF1F5F9),
    surfaceHighlight = Color(0xFFE2E8F0),
    primary = Color(0xFF7C3AED),
    primaryVariant = Color(0xFF6D28D9),
    secondary = Color(0xFF0284C7),
    accentInvestigation = Color(0xFF0891B2),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF94A3B8),
    success = Color(0xFF059669),
    warning = Color(0xFFD97706),
    error = Color(0xFFDC2626),
    border = Color(0xFFCBD5E1),
    divider = Color(0xFFE2E8F0),
    disabled = Color(0xFF94A3B8),
    cardBackground = Color(0xFFFFFFFF),
    packageAlphaColor = Color(0xFF7C3AED),
    packageBetaColor = Color(0xFF0891B2)
)

val LocalDetectiveColors = staticCompositionLocalOf { DarkColorTokens }

@Composable
fun DetectiveTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val tokens = if (darkTheme) DarkColorTokens else LightColorTokens

    val m3Colors = if (darkTheme) {
        darkColorScheme(
            primary = tokens.primary,
            secondary = tokens.secondary,
            background = tokens.background,
            surface = tokens.surface,
            error = tokens.error,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = tokens.textPrimary,
            onSurface = tokens.textPrimary
        )
    } else {
        lightColorScheme(
            primary = tokens.primary,
            secondary = tokens.secondary,
            background = tokens.background,
            surface = tokens.surface,
            error = tokens.error,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = tokens.textPrimary,
            onSurface = tokens.textPrimary
        )
    }

    CompositionLocalProvider(
        LocalDetectiveColors provides tokens
    ) {
        MaterialTheme(
            colorScheme = m3Colors,
            content = content
        )
    }
}

object DetectiveAppTheme {
    val colors: DetectiveColorTokens
        @Composable
        get() = LocalDetectiveColors.current
}
