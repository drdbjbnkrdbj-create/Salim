package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    content: @Composable () -> Unit
) {
    // We intentionally prioritize our refined, custom brand colors over random dynamic color
    // to preserve Salim's signature luxury emerald palette.
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
