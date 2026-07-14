package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = HalabaSoftGreen,
    secondary = HalabaGold,
    tertiary = HalabaCrimson,
    background = ObsidianDark,
    surface = SurfaceDark,
    onPrimary = PureWhite,
    onSecondary = ObsidianDark,
    onTertiary = PureWhite,
    onBackground = Color(0xFFE3E8E3),
    onSurface = Color(0xFFE3E8E3),
    surfaceVariant = Color(0xFF232824),
    onSurfaceVariant = Color(0xFFC2CDC3),
    outline = Color(0xFF8D938E)
)

private val LightColorScheme = lightColorScheme(
    primary = HalabaDarkGreen,
    secondary = HalabaSoftGreen,
    tertiary = HalabaGold,
    background = WarmSand,
    surface = PureWhite,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onTertiary = ObsidianDark,
    onBackground = ObsidianDark,
    onSurface = ObsidianDark,
    surfaceVariant = Color(0xFFE1E5E1),
    onSurfaceVariant = Color(0xFF424943),
    outline = Color(0xFF727973)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
