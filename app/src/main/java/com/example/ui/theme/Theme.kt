package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SalimMint,
    onPrimary = SalimDarkEmerald,
    primaryContainer = SalimDarkEmerald,
    onPrimaryContainer = SalimLightMint,
    secondary = SalimMidGreen,
    onSecondary = Color.White,
    background = SalimDeepForest,
    surface = Color(0xFF162D24),
    surfaceVariant = Color(0xFF1E3A2F),
    onBackground = Color(0xFFE5EDE8),
    onSurface = Color(0xFFE5EDE8),
    error = Color(0xFFEF5350),
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SalimDarkEmerald,
    onPrimary = Color.White,
    primaryContainer = SalimLightMint,
    onPrimaryContainer = SalimDarkEmerald,
    secondary = SalimMidGreen,
    onSecondary = Color.White,
    secondaryContainer = SalimSoftMintBg,
    onSecondaryContainer = SalimDarkEmerald,
    background = SalimBackground,
    surface = SalimSurface,
    surfaceVariant = SalimSurfaceVariant,
    onBackground = SalimTextPrimary,
    onSurface = SalimTextPrimary,
    onSurfaceVariant = SalimTextSecondary,
    outline = SalimBorder,
    error = SalimExpenseRed,
    onError = Color.White
)

@Composable
fun SalimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We intentionally prioritize our refined, custom brand colors over random dynamic color
    // to preserve Salim's signature luxury emerald palette.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
