package com.gym.view.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Premium Gym Management Palette
val GymDarkBackground = Color(0xFF0F172A)
val GymSurfaceDark = Color(0xFF1E293B)
val GymCardDark = Color(0xFF334155)
val GymAccentPrimary = Color(0xFF6366F1) // Indigo
val GymAccentSecondary = Color(0xFF10B981) // Emerald Green (Income)
val GymAccentDanger = Color(0xFFEF4444) // Coral Red (Expenses / Expired)
val GymAccentWarning = Color(0xFFF59E0B) // Amber (Enquire)
val GymTextPrimary = Color(0xFFF8FAFC)
val GymTextSecondary = Color(0xFF94A3B8)

private val DarkColorScheme = darkColorScheme(
    primary = GymAccentPrimary,
    secondary = GymAccentSecondary,
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
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    error = Color(0xFFDC2626),
    onPrimary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

data class AppThemeColors(
    val bg: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val textMain: Color,
    val textMuted: Color,
    val dividerColor: Color
)

@Composable
fun getAppThemeColors(isDarkMode: Boolean): AppThemeColors {
    return if (isDarkMode) {
        AppThemeColors(
            bg = Color(0xFF151518),
            cardBg = Color(0xFF202024),
            cardBorder = Color(0xFF3F3F46),
            textMain = Color(0xFFFFFFFF),
            textMuted = Color(0xFFA1A1AA),
            dividerColor = Color(0xFF27272A)
        )
    } else {
        AppThemeColors(
            bg = Color(0xFFF1F5F9),
            cardBg = Color(0xFFFFFFFF),
            cardBorder = Color(0xFFCBD5E1),
            textMain = Color(0xFF0F172A),
            textMuted = Color(0xFF64748B),
            dividerColor = Color(0xFFE2E8F0)
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
