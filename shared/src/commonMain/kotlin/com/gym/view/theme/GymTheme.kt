package com.gym.view.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Premium Gym Management Palette - Kinetic Obsidian Design System from Stitch
val GymDarkBackground = Color(0xFF0B1326)
val GymSurfaceDark = Color(0xFF171F33)
val GymCardDark = Color(0xFF1E293B)
val GymCardBorderDark = Color(0xFF334155)
val GymAccentPrimary = Color(0xFF6366F1) // Electric Indigo
val GymPrimaryGlow = Color(0xFF8083FF) // Primary container glow
val GymAccentSecondary = Color(0xFF4EDEA3) // Emerald Green (Income / Progress)
val GymAccentDanger = Color(0xFFFF5451) // Coral Red (Expenses / Expired / Burn)
val GymAccentWarning = Color(0xFFF59E0B) // Amber (Enquire / Warnings)
val GymTextPrimary = Color(0xFFDAE2FD) // High-contrast on-surface
val GymTextSecondary = Color(0xFF94A3B8) // Muted on-surface variant
val GymSurfaceContainerHigh = Color(0xFF222A3D)
val GymSurfaceContainerHighest = Color(0xFF2D3449)

private val DarkColorScheme = darkColorScheme(
    primary = GymAccentPrimary,
    secondary = GymAccentSecondary,
    tertiary = GymAccentDanger,
    background = GymDarkBackground,
    surface = GymSurfaceDark,
    error = GymAccentDanger,
    onPrimary = Color.White,
    onBackground = GymTextPrimary,
    onSurface = GymTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5),
    secondary = Color(0xFF059669),
    tertiary = Color(0xFFDC2626),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    error = Color(0xFFDC2626),
    onPrimary = Color.White,
    onBackground = Color(0xFF0B1326),
    onSurface = Color(0xFF0B1326)
)

data class AppThemeColors(
    val bg: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val cardElevated: Color,
    val textMain: Color,
    val textMuted: Color,
    val dividerColor: Color,
    val primary: Color,
    val secondary: Color,
    val danger: Color,
    val warning: Color
)

@Composable
fun getAppThemeColors(isDarkMode: Boolean): AppThemeColors {
    return if (isDarkMode) {
        AppThemeColors(
            bg = GymDarkBackground,
            cardBg = GymSurfaceDark,
            cardBorder = GymCardBorderDark,
            cardElevated = GymSurfaceContainerHigh,
            textMain = GymTextPrimary,
            textMuted = GymTextSecondary,
            dividerColor = Color(0xFF222A3D),
            primary = GymAccentPrimary,
            secondary = GymAccentSecondary,
            danger = GymAccentDanger,
            warning = GymAccentWarning
        )
    } else {
        AppThemeColors(
            bg = Color(0xFFF1F5F9),
            cardBg = Color(0xFFFFFFFF),
            cardBorder = Color(0xFFCBD5E1),
            cardElevated = Color(0xFFF8FAFC),
            textMain = Color(0xFF0F172A),
            textMuted = Color(0xFF64748B),
            dividerColor = Color(0xFFE2E8F0),
            primary = Color(0xFF4F46E5),
            secondary = Color(0xFF059669),
            danger = Color(0xFFDC2626),
            warning = Color(0xFFD97706)
        )
    }
}

@Composable
fun GymTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
